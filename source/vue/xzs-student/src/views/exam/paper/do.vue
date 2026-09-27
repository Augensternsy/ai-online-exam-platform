<template>
  <div>
    <el-row  class="do-exam-title">
      <el-col :span="24">
        <span :key="item.itemOrder"  v-for="item in answer.answerItems">
             <el-tag :type="questionCompleted(item.completed)" class="do-exam-title-tag" @click="goAnchor('#question-'+item.itemOrder)">{{item.itemOrder}}</el-tag>
        </span>
        <span class="do-exam-time">
          <label>剩余时间：</label>
          <label>{{formatSeconds(remainTime)}}</label>
        </span>
      </el-col>
    </el-row>
    <el-row  class="do-exam-title-hidden">
      <el-col :span="24">
        <span :key="item.itemOrder"  v-for="item in answer.answerItems">
             <el-tag  class="do-exam-title-tag" >{{item.itemOrder}}</el-tag>
        </span>
        <span class="do-exam-time">
          <label>剩余时间：</label>
        </span>
      </el-col>
    </el-row>
    <el-container  class="app-item-contain">
      <el-header class="align-center">
        <h1>{{form.name}}</h1>
        <div>
          <span class="question-title-padding">试卷总分：{{form.score}}</span>
          <span class="question-title-padding">考试时间：{{form.suggestTime}}分钟</span>
        </div>
      </el-header>
      <el-main>
        <el-form :model="form" ref="form" v-loading="formLoading" label-width="100px">
          <el-row :key="index"  v-for="(titleItem,index) in form.titleItems">
            <h3>{{titleItem.name}}</h3>
            <el-card class="exampaper-item-box" v-if="titleItem.questionItems.length!==0">
              <el-form-item :key="questionItem.itemOrder" :label="questionItem.itemOrder+'.'"
                            v-for="questionItem in titleItem.questionItems"
                            class="exam-question-item" label-width="50px" :id="'question-'+ questionItem.itemOrder">
                <QuestionEdit :qType="questionItem.questionType" :question="questionItem"
                              :answer="answer.answerItems[questionItem.itemOrder-1]"/>
              </el-form-item>
            </el-card>
          </el-row>
           <el-row class="do-align-center">
             <el-button type="primary" @click="submitForm">提交</el-button>
             <el-button @click="retrySaveAnswer">手动保存</el-button>
             <el-button>取消</el-button>
             <el-tag :type="networkStatusType" style="margin-left: 20px;">
               {{ networkStatusText }}
             </el-tag>
           </el-row>
        </el-form>
      </el-main>
      <el-dialog title="提示" :visible.sync="tipsFlag" width="480px" class="commonDialog multi clickLight" center :close-on-click-modal="false">
      <div class="dialogTipsbox" v-if="tips===1">你还有试题未作答，确认要交卷？</div>
      <div class="dialogTipsbox" v-if="tips===2">
        最多只能切屏{{switchPage.switchPageTimes}}次，你还可切换{{switchPage.remaTimes}}次，
        <br />
        超过{{switchPage.switchPageTimes}}次将强行交卷！
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="tipsFlag = false" v-if="tips===1">取 消</el-button>
      </span>
</el-dialog>
    </el-container>
    <div class="monitor-container">
      <ExamMonitor
        ref="examMonitor"
        :max-violations="5"
        :sample-rate="500"
        :leave-threshold="3"
        :look-around-threshold="0.2"
        :face-width-threshold="0.3"
        :consecutive-threshold="3"
        @violation="handleViolation"
        @exam-interrupted="handleExamInterrupted"
        @confirm-interrupt="handleConfirmInterrupt"
      />
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import { formatSeconds } from '@/utils'
import QuestionEdit from '../components/QuestionEdit'
import ExamMonitor from '@/components/ExamMonitor'
import examPaperApi from '@/api/examPaper'
import examPaperAnswerApi from '@/api/examPaperAnswer'
import { post as postRequest } from '@/utils/request'
import DEMO_MODE from '@/mock'

export default {
  components: { QuestionEdit, ExamMonitor },
  props: ['src'],
  data () {
    return {
      form: {},
      formLoading: false,
      answer: {
        questionId: null,
        doTime: 0,
        answerItems: []
      },
      timer: null,
      autoSaveTimer: null,
      remainTime: 0,
      examAnswerId: null,
      tips: 0,
      tipsFlag: false,
      copyPasteCallback: null,
      switchPage: {
        switchPageTimes: 5,
        remaTimes: 5
      },
      violationCount: 0,
      isOnline: typeof navigator !== 'undefined' ? navigator.onLine : true
    }
  },
  created () {
    let id = this.$route.query.id
    let _this = this
    if (id && parseInt(id) !== 0) {
      _this.formLoading = true
      examPaperApi.select(id).then(re => {
        _this.form = re.response
        _this.remainTime = re.response.suggestTime * 60
        _this.initAnswer()
        _this.startExam().then(() => {
          _this.timeReduce()
          _this.startAutoSave()
        })
        _this.formLoading = false
        _this.forbidCopyPaste()
      })
    }
  },
  mounted () {
    var self = this
    window.addEventListener('visibilitychange', function () {
      if (document.visibilityState === 'hidden') {
      } else if (document.visibilityState === 'visible') {
        // Showcase Demo 不做切屏检测
        if (DEMO_MODE) return
        self.violationCount = self.violationCount + 1
        self.recordViolation('切屏', '检测到切屏行为', self.violationCount)

        if (self.violationCount >= 5) {
          self.$alert('已经违规操作5次,系统自动交卷!', '提示', {
            confirmButtonText: '确定',
            type: 'warning'
          }).then(() => {
            self.src = 'user'
            self.submitForm()
          })
        } else {
          self.$alert('禁止切换界面,当前违规操作' + self.violationCount + '次,违规操作5次自动交卷!', '警告', {
            confirmButtonText: '我知道了',
            type: 'warning'
          })
        }
      }
    })

    // 监听浏览器网络状态，更新网络指示
    self.handleNetworkChange = function () {
      self.isOnline = navigator.onLine
    }
    window.addEventListener('online', self.handleNetworkChange)
    window.addEventListener('offline', self.handleNetworkChange)
  },
  beforeDestroy () {
    window.clearInterval(this.timer)
    window.clearInterval(this.autoSaveTimer)
    this.removeCopyPaste()
    if (this.handleNetworkChange) {
      window.removeEventListener('online', this.handleNetworkChange)
      window.removeEventListener('offline', this.handleNetworkChange)
    }
  },
  computed: {
    ...mapGetters('enumItem', ['enumFormat']),
    ...mapState('enumItem', {
      doCompletedTag: state => state.exam.question.answer.doCompletedTag
    }),
    networkStatusType () {
      return this.isOnline ? 'success' : 'danger'
    },
    networkStatusText () {
      return this.isOnline ? '网络在线' : '网络已断开'
    }
  },
  methods: {
    handleViolation (violation) {
      this.recordViolation(violation.type, violation.type + '检测', violation.count)
    },
    handleExamInterrupted (interruptData) {
      this.$alert('考试已中断：' + interruptData.reason, '提示', {
        confirmButtonText: '确定',
        type: 'error'
      }).then(() => {
        this.src = 'user'
        this.submitForm()
      })
    },
    handleConfirmInterrupt () {
      this.src = 'user'
      this.submitForm()
    },
    recordViolation (violationType, violationDetail, violationCount) {
      // 先保存到本地缓存（弱网兜底）
      this.saveViolationToLocal(violationType, violationDetail, violationCount)

      // Demo 模式无真实后端，仅保留本地记录
      if (DEMO_MODE) return

      // 尝试上报到后端
      postRequest('/api/student/exam/paper/recordViolation', {
        examPaperId: this.form.id,
        examPaperName: this.form.name,
        violationType: violationType,
        violationDetail: violationDetail,
        violationCount: violationCount
      }).then(response => {
        console.log('违规记录已保存到服务器')
        // 上报成功，清除本地缓存
        this.clearViolationCache()
      }).catch(error => {
        console.error('保存违规记录失败，已保存到本地', error)
        // 弱网情况下，提示用户
        this.$message.warning('网络异常，违规记录已保存到本地，网络恢复后将自动上传')
        // 启动重试机制
        this.retryUploadViolations()
      })
    },
    saveViolationToLocal (type, detail, count) {
      try {
        const cacheKey = `exam_violations_${this.form.id}`
        let violations = JSON.parse(localStorage.getItem(cacheKey) || '[]')
        violations.push({
          type,
          detail,
          count,
          timestamp: Date.now(),
          synced: false
        })
        localStorage.setItem(cacheKey, JSON.stringify(violations))
        console.log('违规记录已保存到本地缓存')
      } catch (e) {
        console.error('保存违规记录到本地失败', e)
      }
    },
    clearViolationCache () {
      try {
        const cacheKey = `exam_violations_${this.form.id}`
        localStorage.removeItem(cacheKey)
      } catch (e) {
        console.error('清除违规记录缓存失败', e)
      }
    },
    retryUploadViolations () {
      const cacheKey = `exam_violations_${this.form.id}`
      let violations = JSON.parse(localStorage.getItem(cacheKey) || '[]')

      if (violations.length === 0) return

      let retryIndex = 0
      const maxRetries = 3

      const uploadNext = () => {
        if (retryIndex >= violations.length) {
          // 所有记录都已重试
          this.$message.success('违规记录已全部上传')
          this.clearViolationCache()
          return
        }

        const violation = violations[retryIndex]
        if (violation.synced) {
          retryIndex++
          uploadNext()
          return
        }

        postRequest('/api/student/exam/paper/recordViolation', {
          examPaperId: this.form.id,
          examPaperName: this.form.name,
          violationType: violation.type,
          violationDetail: violation.detail,
          violationCount: violation.count
        }).then(response => {
          violation.synced = true
          localStorage.setItem(cacheKey, JSON.stringify(violations))
          retryIndex++
          setTimeout(uploadNext, 1000)
        }).catch(error => {
          console.error(`第${retryIndex + 1}条违规记录上传失败`, error)
          retryIndex++
          setTimeout(uploadNext, 2000)
        })
      }

      uploadNext()
    },
    formatSeconds (theTime) {
      return formatSeconds(theTime)
    },
    timeReduce () {
      let _this = this
      this.timer = setInterval(function () {
        if (_this.remainTime <= 0) {
          _this.submitForm()
        } else {
          ++_this.answer.doTime
          --_this.remainTime
        }
      }, 1000)
    },
    questionCompleted (completed) {
      return this.enumFormat(this.doCompletedTag, completed)
    },
    goAnchor (selector) {
      this.$el.querySelector(selector).scrollIntoView({ behavior: 'instant', block: 'center', inline: 'nearest' })
    },
    initAnswer () {
      this.answer.id = this.form.id
      let titleItemArray = this.form.titleItems
      for (let tIndex in titleItemArray) {
        let questionArray = titleItemArray[tIndex].questionItems
        for (let qIndex in questionArray) {
          let question = questionArray[qIndex]
          this.answer.answerItems.push({ questionId: question.id, content: null, contentArray: [], completed: false, itemOrder: question.itemOrder })
        }
      }
    },
    startExam () {
      let _this = this
      return examPaperAnswerApi.startExam({ examPaperId: this.form.id }).then(re => {
        if (re.code === 1) {
          _this.examAnswerId = re.response.id
          _this.answer.id = re.response.id
          console.log('考试已开始，答题记录ID:', _this.examAnswerId)
        } else {
          _this.$message.error('开始考试失败：' + re.message)
        }
      }).catch(e => {
        _this.$message.error('开始考试失败')
      })
    },
    startAutoSave () {
      let _this = this
      this.autoSaveTimer = setInterval(function () {
        _this.autoSaveAnswer()
      }, 60000)
    },
    autoSaveAnswer () {
      let _this = this
      if (!_this.examAnswerId) {
        return
      }
      let saveData = {
        examPaperId: _this.form.id,
        doTime: _this.answer.doTime,
        answerItems: _this.answer.answerItems
      }

      // 先保存到本地缓存（双保险）
      _this.saveToLocalCache(saveData)

      // 再尝试保存到服务器
      examPaperAnswerApi.saveAnswer(saveData).then(re => {
        if (re.code === 1) {
          console.log('答案已自动保存到服务器')
          // 服务器保存成功，清除本地缓存
          _this.clearLocalCache()
        }
      }).catch(e => {
        console.error('自动保存到服务器失败', e)
        // 服务器保存失败，本地缓存已存在，提示用户
        _this.$message.warning('网络异常，答案已保存到本地，网络恢复后将自动上传')
      })
    },
    saveToLocalCache (data) {
      try {
        const cacheKey = `exam_answer_${this.examAnswerId}`
        const cacheData = {
          ...data,
          timestamp: Date.now(),
          version: 1
        }
        localStorage.setItem(cacheKey, JSON.stringify(cacheData))
        console.log('答案已保存到本地缓存')
      } catch (e) {
        console.error('本地缓存保存失败', e)
      }
    },
    clearLocalCache () {
      try {
        const cacheKey = `exam_answer_${this.examAnswerId}`
        localStorage.removeItem(cacheKey)
        console.log('本地缓存已清除')
      } catch (e) {
        console.error('清除本地缓存失败', e)
      }
    },
    restoreFromLocalCache () {
      try {
        const cacheKey = `exam_answer_${this.examAnswerId}`
        const cached = localStorage.getItem(cacheKey)
        if (cached) {
          const cacheData = JSON.parse(cached)
          console.log('从本地缓存恢复答案，时间戳:', new Date(cacheData.timestamp).toLocaleString())
          // 恢复答案数据
          this.answer.doTime = cacheData.doTime
          this.answer.answerItems = cacheData.answerItems
          this.$message.info('已从本地缓存恢复未保存的答案')
          return true
        }
      } catch (e) {
        console.error('从本地缓存恢复失败', e)
      }
      return false
    },
    retrySaveAnswer () {
      let _this = this
      if (!_this.examAnswerId) {
        return
      }

      const saveData = {
        examPaperId: _this.form.id,
        doTime: _this.answer.doTime,
        answerItems: _this.answer.answerItems
      }

      let retryCount = 0
      const maxRetries = 3

      function attemptRetry () {
        retryCount++
        console.log(`尝试重新保存答案 (${retryCount}/${maxRetries})`)

        examPaperAnswerApi.saveAnswer(saveData).then(re => {
          if (re.code === 1) {
            console.log('答案重新保存成功')
            _this.clearLocalCache()
            _this.$message.success('答案已重新保存到服务器')
          }
        }).catch(e => {
          console.error(`第${retryCount}次重试失败`, e)
          if (retryCount < maxRetries) {
            setTimeout(attemptRetry, 2000 * retryCount) // 递增延迟重试
          } else {
            _this.$message.error('重试失败，答案仍保存在本地，请检查网络后手动保存')
          }
        })
      }

      attemptRetry()
    },
    submitForm () {
      let _this = this
      this.alertFlag = true
      window.clearInterval(_this.timer)
      window.clearInterval(_this.autoSaveTimer)
      _this.formLoading = true

      let submitData = {
        id: _this.examAnswerId,
        doTime: _this.answer.doTime,
        answerItems: _this.answer.answerItems
      }

      examPaperAnswerApi.submitExam(submitData).then(re => {
        if (re.code === 1) {
          _this.$alert('试卷得分：' + re.response + '分', '考试结果', {
            confirmButtonText: '返回考试记录',
            callback: action => {
              _this.$router.push('/record/index')
            }
          })
        } else {
          if (re.code === 2) {
            // 试卷已提交（如在其他标签页交过卷），停留在答题页只会重复报错，直接引导返回考试记录
            _this.$alert(re.message, '提示', {
              confirmButtonText: '返回考试记录',
              callback: action => {
                _this.$router.push('/record/index')
              }
            })
          } else {
            _this.$message.error(re.message)
          }
        }
        _this.formLoading = false
      }).catch(e => {
        _this.formLoading = false
      })
    },
    copyPasteEvent (e) {
      e.preventDefault()
      if (e.type === 'copy' || e.type === 'paste') {
        alert('禁止复制粘贴')
        this.copyPasteCallback && this.copyPasteCallback()
      }
    },
    forbidCopyPaste (callback) {
      document.addEventListener('copy', this.copyPasteEvent)
      document.addEventListener('paste', this.copyPasteEvent)
      this.copyPasteCallback = callback
    },
    removeCopyPaste () {
      document.removeEventListener('copy', this.copyPasteEvent)
      document.removeEventListener('paste', this.copyPasteEvent)
      this.copyPasteCallback = null
    }
  }
}
</script>

<style lang="scss" scoped>
  .align-center {
    text-align: center
  }

  .exam-question-item {
    padding: 10px;

    .el-form-item__label {
      font-size: 15px !important;
    }
  }

  .question-title-padding {
    padding-left: 25px;
    padding-right: 25px;
  }

  .monitor-container {
    position: fixed;
    bottom: 20px;
    right: 20px;
    z-index: 1000;
  }
</style>
