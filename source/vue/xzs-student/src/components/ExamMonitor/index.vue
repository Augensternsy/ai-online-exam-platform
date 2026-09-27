<template>
  <div class="exam-monitor">
    <!-- 摄像头预览 -->
    <div class="video-container" :class="containerClass" v-if="!demoMode">
      <video ref="video" :width="videoWidth" :height="videoHeight" autoplay muted playsinline style="display: block;"></video>
      <!-- 人脸检测框画布：与视频同分辨率叠加，检测到的人脸实时画框 -->
      <canvas ref="canvas" class="face-canvas"></canvas>

      <!-- 监控状态指示器 -->
      <div class="monitor-status">
        <div class="status-indicator" :class="statusClass"></div>
        <span class="status-text">{{ statusText }}</span>
      </div>

      <!-- 违规计数显示 -->
      <div class="violation-counter" v-if="violationCount > 0">
        <el-badge :value="violationCount" class="item">
          <i class="el-icon-warning"></i>
        </el-badge>
      </div>
    </div>

    <!-- Demo 模式提示：不请求摄像头 -->
    <div class="monitor-demo-badge" v-else>
      <i class="el-icon-info"></i>
      <span>Showcase Demo 未启用人脸监控</span>
    </div>

    <!-- 权限请求对话框 -->
    <el-dialog
      title="摄像头权限请求"
      :visible.sync="permissionDialogVisible"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      center>
      <div class="permission-dialog-content">
        <i class="el-icon-camera" style="font-size: 48px; color: #409EFF;"></i>
        <p>考试需要开启摄像头进行防作弊监控</p>
        <p class="permission-tip">请点击浏览器地址栏左侧的"锁"图标，允许摄像头权限</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="retryPermission">我已开启权限，重试</el-button>
      </span>
    </el-dialog>

    <!-- 强制中断蒙层 -->
    <div class="exam-interrupt-mask" v-if="isInterrupted">
      <div class="interrupt-content">
        <i class="el-icon-circle-close" style="font-size: 64px; color: #F56C6C;"></i>
        <h2>考试已中断</h2>
        <p>{{ interruptReason }}</p>
        <el-button type="primary" @click="handleInterruptConfirm">确认</el-button>
      </div>
    </div>

    <!-- 警告提示 -->
    <el-dialog
      title="警告"
      :visible.sync="warningDialogVisible"
      width="400px"
      :close-on-click-modal="false"
      center>
      <div class="warning-content">
        <i class="el-icon-warning" style="font-size: 48px; color: #E6A23C;"></i>
        <p>{{ warningMessage }}</p>
        <p class="warning-count">违规次数：{{ violationCount }}/{{ maxViolations }}</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="warningDialogVisible = false">我知道了</el-button>
      </span>
    </el-dialog>

    <!-- 摄像头无画面提示（流已获取但持续黑屏） -->
    <el-dialog
      title="摄像头无画面"
      :visible.sync="noVideoDialogVisible"
      width="460px"
      :close-on-click-modal="false"
      center>
      <div class="warning-content">
        <i class="el-icon-video-camera" style="font-size: 48px; color: #E6A23C;"></i>
        <p>摄像头已开启，但没有检测到视频画面</p>
        <p class="permission-tip">请检查：摄像头隐私挡板是否打开、是否被其他程序占用，然后点击重新检测</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="retryVideo">重新检测</el-button>
      </span>
    </el-dialog>

    <!-- 人脸位置提醒（校准阶段持续识别不到人脸，不记违规） -->
    <el-dialog
      title="未检测到人脸"
      :visible.sync="faceGuideDialogVisible"
      width="460px"
      :close-on-click-modal="false"
      center>
      <div class="warning-content">
        <i class="el-icon-user" style="font-size: 48px; color: #409EFF;"></i>
        <p>已开启监控，但暂时没有识别到您的人脸</p>
        <p class="permission-tip">请正对摄像头、让面部完整出现在画面内，并注意光线充足</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="faceGuideDialogVisible = false">我知道了</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
/* global tracking */
require('tracking/build/tracking-min.js')
require('tracking/build/data/face-min.js')

export default {
  name: 'ExamMonitor',
  props: {
    // 最大违规次数
    maxViolations: {
      type: Number,
      default: 5
    },
    // 采样频率（毫秒）
    sampleRate: {
      type: Number,
      default: 500
    },
    // 离开判定时间（秒）
    leaveThreshold: {
      type: Number,
      default: 3
    },
    // 左顾右盼偏移阈值（百分比）
    lookAroundThreshold: {
      type: Number,
      default: 0.2
    },
    // 人脸宽度缩小阈值（百分比）
    faceWidthThreshold: {
      type: Number,
      default: 0.3
    },
    // 连续检测次数阈值
    consecutiveThreshold: {
      type: Number,
      default: 3
    }
  },
  data () {
    return {
      // Showcase Demo：不请求摄像头
      demoMode: process.env.VUE_APP_DEMO_MODE === 'true',
      videoWidth: 320,
      videoHeight: 240,
      isMonitoring: false,
      isInterrupted: false,
      interruptReason: '',
      permissionDialogVisible: false,
      warningDialogVisible: false,
      warningMessage: '',
      violationCount: 0,
      tracker: null,
      trackerTask: null,
      stream: null,

      // 行为判定相关
      baselineCenterX: 0,
      baselineFaceWidth: 0,
      isBaselineSet: false,
      consecutiveViolations: 0,
      lastViolationTime: 0,
      lastNoFaceViolation: 0,
      leaveStartTime: 0,
      isLeaving: false,

      // 定时器
      monitorTimer: null,
      retryTimer: null,
      videoCheckTimer: null,

      // 当前帧检测状态（供界面实时反馈是否识别到人脸）
      currentFaceCount: 0,
      noVideoDialogVisible: false,

      // 校准阶段（基准未建立）持续无脸的提醒
      noFaceStartTime: 0,
      lastFaceGuideTime: 0,
      faceGuideDialogVisible: false,

      // 设备占用重试
      retryCount: 0,
      maxRetries: 3
    }
  },
  computed: {
    containerClass () {
      return {
        'monitor-active': this.isMonitoring,
        // 已建立基准后当前帧丢失人脸：红色边框脉冲提示
        'face-lost': this.isMonitoring && this.isBaselineSet && this.currentFaceCount === 0
      }
    },
    statusClass () {
      if (this.isInterrupted) return 'status-error'
      if (this.violationCount >= this.maxViolations) return 'status-danger'
      if (!this.isMonitoring) return 'status-warning'
      if (this.currentFaceCount === 0) {
        return this.isBaselineSet ? 'status-danger' : 'status-warning'
      }
      return this.violationCount > 0 ? 'status-warning' : 'status-normal'
    },
    statusText () {
      if (this.isInterrupted) return '考试已中断'
      if (this.violationCount >= this.maxViolations) return '违规次数已达上限'
      if (!this.isMonitoring) return '等待摄像头...'
      if (this.currentFaceCount > 1) return `检测到${this.currentFaceCount}人`
      if (this.currentFaceCount === 1) {
        const text = this.isBaselineSet ? '人脸已识别' : '识别成功，正在校准'
        return this.violationCount > 0 ? `${text}（违规${this.violationCount}次）` : text
      }
      // 当前帧无人脸
      return this.isBaselineSet ? '未检测到人脸' : '正在识别人脸，请正对屏幕'
    }
  },
  mounted () {
    // Showcase Demo 不请求摄像头、不启用人脸监控
    if (this.demoMode) return
    this.initMonitor()
  },
  beforeDestroy () {
    this.stopMonitor()
  },
  methods: {
    // 初始化监控
    async initMonitor () {
      try {
        await this.requestCameraPermission()
        this.startMonitoring()
      } catch (error) {
        this.handleCameraError(error)
      }
    },

    // 请求摄像头权限
    async requestCameraPermission () {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: {
          width: { ideal: this.videoWidth },
          height: { ideal: this.videoHeight },
          facingMode: 'user'
        },
        audio: false
      })
      const video = this.$refs.video
      video.srcObject = this.stream
      // muted + 显式 play，规避浏览器自动播放策略导致的黑屏/暂停
      video.muted = true
      await video.play().catch(() => {})
      // 等待视频元数据，确保 startMonitoring 时能拿到真实分辨率
      if (!video.videoWidth) {
        await new Promise(resolve => {
          const timer = setTimeout(resolve, 8000)
          video.addEventListener('loadedmetadata', () => {
            clearTimeout(timer)
            resolve()
          }, { once: true })
        })
      }
      this.permissionDialogVisible = false
      this.noVideoDialogVisible = false
      this.retryCount = 0
    },

    // 开始监控
    startMonitoring () {
      const video = this.$refs.video
      if (!video.srcObject) return

      this.isMonitoring = true

      // 画布与视频真实分辨率对齐（流可能不是 320x240），CSS 再拉伸到显示尺寸
      const canvas = this.$refs.canvas
      canvas.width = video.videoWidth || this.videoWidth
      canvas.height = video.videoHeight || this.videoHeight

      // 初始化 Tracking.js。注意：不传 { camera: true }——
      // 该选项会让 tracking 用旧 API 再次打开摄像头并覆盖 srcObject，造成双流争抢黑屏
      this.tracker = new tracking.ObjectTracker('face')
      this.tracker.setInitialScale(4)
      this.tracker.setStepSize(2)
      this.tracker.setEdgesDensity(0.1)

      this.trackerTask = tracking.track(video, this.tracker)

      this.tracker.on('track', this.handleTrackEvent)

      // 设置采样定时器
      this.monitorTimer = setInterval(() => {
        this.performBehaviorAnalysis()
      }, this.sampleRate)

      // 黑屏诊断：8 秒后仍无视频帧，或帧像素几乎全黑（隐私挡板/被占用），
      // 多为隐私挡板未开/被其他程序占用
      this.videoCheckTimer = setTimeout(() => {
        if (this.isMonitoring && this.$refs.video.srcObject
          && (this.$refs.video.readyState < 2 || !this.$refs.video.videoWidth || this.isFrameBlack())) {
          this.noVideoDialogVisible = true
        }
      }, 8000)
    },

    // 采样当前视频帧，判断是否几乎全黑（物理挡板盖住时流仍在、但像素全黑）
    isFrameBlack () {
      const video = this.$refs.video
      if (!video.videoWidth) return false
      try {
        const probe = document.createElement('canvas')
        probe.width = 64
        probe.height = 48
        const ctx = probe.getContext('2d')
        ctx.drawImage(video, 0, 0, probe.width, probe.height)
        const data = ctx.getImageData(0, 0, probe.width, probe.height).data
        let nonBlack = 0
        let sum = 0
        for (let i = 0; i < data.length; i += 4) {
          const lum = (data[i] + data[i + 1] + data[i + 2]) / 3
          sum += lum
          if (lum > 10) nonBlack++
        }
        const total = data.length / 4
        return nonBlack / total < 0.01 && sum / total < 6
      } catch (e) {
        return false
      }
    },

    // 处理追踪事件
    handleTrackEvent (event) {
      const faces = event.data
      this.currentFaceCount = faces.length
      this.drawFaces(faces)

      if (faces.length === 0) {
        // 无人脸
        this.handleNoFace()
      } else if (faces.length === 1) {
        // 单人脸
        this.handleSingleFace(faces[0])
      } else {
        // 多人脸
        this.handleMultipleFaces(faces)
      }
    },

    // 在叠加画布上绘制人脸框，用户可直观确认系统识别到了人脸
    drawFaces (faces) {
      const canvas = this.$refs.canvas
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      ctx.strokeStyle = '#00ff00'
      ctx.lineWidth = 2
      faces.forEach(face => {
        ctx.strokeRect(face.x, face.y, face.width, face.height)
      })
    },

    // 处理无人脸
    handleNoFace () {
      const now = Date.now()

      // 基准点尚未建立（摄像头刚启动、还没检测到人脸）时不判定违规，避免开局误判；
      // 但持续识别不到人脸要弹窗提醒，否则用户会一直卡在“正在识别人脸”
      if (!this.isBaselineSet) {
        if (!this.noFaceStartTime) {
          this.noFaceStartTime = now
        } else if (now - this.noFaceStartTime >= 12000 &&
          now - this.lastFaceGuideTime > 15000 &&
          !this.noVideoDialogVisible) {
          this.faceGuideDialogVisible = true
          this.lastFaceGuideTime = now
        }
        return
      }

      if (!this.isLeaving) {
        this.isLeaving = true
        this.leaveStartTime = now
      } else {
        const leaveDuration = (now - this.leaveStartTime) / 1000
        // 持续无脸超过阈值才记违规，且同类违规 30 秒内不重复计数
        if (leaveDuration >= this.leaveThreshold && now - this.lastNoFaceViolation > 30000) {
          this.recordViolation('考生离开座位')
          this.lastNoFaceViolation = now
          // 保持 isLeaving=true：持续无脸时按冷却时间重复计数，而不是每 3 秒计一次
        }
      }
    },

    // 处理单人脸
    handleSingleFace (face) {
      this.isLeaving = false
      // 检测到人脸：清除无脸计时，并自动关闭“未检测到人脸”提醒
      this.noFaceStartTime = 0
      this.faceGuideDialogVisible = false

      if (!this.isBaselineSet) {
        // 第一次检测到人脸时建立基准点，比固定延时一次性采样更可靠
        this.baselineCenterX = face.x + face.width / 2
        this.baselineFaceWidth = face.width
        this.isBaselineSet = true
      } else {
        this.checkLookAround(face)
      }
    },

    // 处理多人脸
    handleMultipleFaces (faces) {
      this.recordViolation('检测到多人')
    },

    // 检查左顾右盼
    checkLookAround (face) {
      // face 坐标基于视频真实分辨率，分母同样使用真实像素宽度，避免比例失真
      const realWidth = (this.$refs.video && this.$refs.video.videoWidth) || this.videoWidth
      const currentCenterX = face.x + face.width / 2
      const offsetX = Math.abs(currentCenterX - this.baselineCenterX)
      const offsetRatio = offsetX / realWidth

      const widthRatio = Math.abs(face.width - this.baselineFaceWidth) / this.baselineFaceWidth

      if (offsetRatio > this.lookAroundThreshold && widthRatio > this.faceWidthThreshold) {
        this.consecutiveViolations++

        if (this.consecutiveViolations >= this.consecutiveThreshold) {
          this.recordViolation('疑似左顾右盼')
          this.consecutiveViolations = 0
        }
      } else {
        this.consecutiveViolations = 0
      }
    },

    // 行为分析
    performBehaviorAnalysis () {
      // 这里可以添加额外的行为分析逻辑
      // 例如：头部姿态估计、视线追踪等
    },

    // 记录违规
    recordViolation (reason) {
      const now = Date.now()
      if (now - this.lastViolationTime < 2000) {
        // 2秒内不重复记录相同违规
        return
      }

      this.violationCount++
      this.lastViolationTime = now

      // 发送违规事件到后端
      this.$emit('violation', {
        type: reason,
        count: this.violationCount,
        timestamp: now
      })

      if (this.violationCount >= this.maxViolations) {
        this.interruptExam('违规次数已达上限，考试自动结束')
      } else {
        this.showWarning(`检测到${reason}，请注意考试纪律`)
      }
    },

    // 显示警告
    showWarning (message) {
      this.warningMessage = message
      this.warningDialogVisible = true
    },

    // 中断考试
    interruptExam (reason) {
      this.isInterrupted = true
      this.interruptReason = reason
      this.stopMonitor()

      this.$emit('exam-interrupted', {
        reason: reason,
        violationCount: this.violationCount
      })
    },

    // 处理中断确认
    handleInterruptConfirm () {
      this.$emit('confirm-interrupt')
    },

    // 停止监控
    stopMonitor () {
      this.isMonitoring = false

      if (this.monitorTimer) {
        clearInterval(this.monitorTimer)
        this.monitorTimer = null
      }

      if (this.videoCheckTimer) {
        clearTimeout(this.videoCheckTimer)
        this.videoCheckTimer = null
      }

      if (this.trackerTask) {
        this.trackerTask.stop()
        this.trackerTask = null
      }

      if (this.stream) {
        this.stream.getTracks().forEach(track => track.stop())
        this.stream = null
      }

      this.currentFaceCount = 0
      this.noFaceStartTime = 0
      this.drawFaces([])
    },

    // 处理摄像头错误
    handleCameraError (error) {
      console.error('摄像头错误:', error)

      if (error.name === 'NotAllowedError') {
        // 权限拒绝
        this.permissionDialogVisible = true
      } else if (error.name === 'NotReadableError') {
        // 设备占用
        this.handleDeviceOccupied()
      } else if (error.name === 'NotFoundError') {
        // 设备不存在
        this.interruptExam('未检测到摄像头设备')
      } else {
        this.interruptExam('摄像头初始化失败: ' + error.message)
      }
    },

    // 处理设备占用
    handleDeviceOccupied () {
      if (this.retryCount < this.maxRetries) {
        this.retryCount++
        this.$message.warning(`摄像头被占用，正在重试（${this.retryCount}/${this.maxRetries}）`)

        this.retryTimer = setTimeout(() => {
          this.initMonitor()
        }, 3000)
      } else {
        this.interruptExam('摄像头被占用，请关闭其他使用摄像头的程序后重试')
      }
    },

    // 重试权限
    retryPermission () {
      this.initMonitor()
    },

    // 黑屏提示后的重新检测：彻底停掉旧流再重新初始化
    retryVideo () {
      this.noVideoDialogVisible = false
      this.stopMonitor()
      this.initMonitor()
    },

    // 重置监控
    resetMonitor () {
      this.violationCount = 0
      this.isInterrupted = false
      this.interruptReason = ''
      this.isBaselineSet = false
      this.consecutiveViolations = 0
      this.isLeaving = false
      this.lastNoFaceViolation = 0
      this.retryCount = 0
      this.currentFaceCount = 0
      this.noFaceStartTime = 0
      this.lastFaceGuideTime = 0
      this.faceGuideDialogVisible = false

      this.stopMonitor()
      this.initMonitor()
    }
  }
}
</script>

<style scoped>
.exam-monitor {
  position: relative;
}

.monitor-demo-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 12px;
  color: #909399;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  white-space: nowrap;
}

.video-container {
  position: relative;
  display: inline-block;
  border: 2px solid #DCDFE6;
  border-radius: 4px;
  overflow: hidden;
  transition: border-color 0.3s;
}

.video-container.monitor-active {
  border-color: #67C23A;
}

.video-container.face-lost {
  border-color: #F56C2C;
  animation: face-lost-pulse 1.2s infinite;
}

@keyframes face-lost-pulse {
  0% { box-shadow: 0 0 0 0 rgba(245, 108, 44, 0.5); }
  70% { box-shadow: 0 0 0 8px rgba(245, 108, 44, 0); }
  100% { box-shadow: 0 0 0 0 rgba(245, 108, 44, 0); }
}

.face-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.monitor-status {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  padding: 4px 8px;
  border-radius: 4px;
  color: white;
  font-size: 12px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-normal {
  background-color: #67C23A;
}

.status-warning {
  background-color: #E6A23C;
}

.status-danger {
  background-color: #F56C6C;
}

.status-error {
  background-color: #909399;
}

.violation-counter {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #F56C6C;
  font-size: 20px;
}

.permission-dialog-content {
  text-align: center;
  padding: 20px;
}

.permission-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 10px;
}

.exam-interrupt-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.interrupt-content {
  background: white;
  padding: 40px;
  border-radius: 8px;
  text-align: center;
  max-width: 400px;
}

.interrupt-content h2 {
  margin: 20px 0 10px;
  color: #F56C6C;
}

.interrupt-content p {
  color: #606266;
  margin-bottom: 20px;
}

.warning-content {
  text-align: center;
  padding: 20px;
}

.warning-count {
  color: #F56C6C;
  font-weight: bold;
  margin-top: 10px;
}
</style>
