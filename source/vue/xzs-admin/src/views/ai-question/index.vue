<template>
  <div class="ai-question-container">
    <!-- AI Agent 自然语言出题 - v2 -->
    <el-card class="box-card agent-card">
      <div slot="header" class="clearfix">
        <span><i class="el-icon-magic-stick"></i> AI Agent 自然语言出题</span>
        <span style="float: right; color: #909399; font-size: 12px;">一句话描述需求，Agent 自动解析并生成试卷</span>
      </div>

      <el-input
        v-model="agentInput"
        type="textarea"
        :rows="3"
        placeholder="例如：生成一套 Java 基础，中等难度，20 道题，包含单选/多选/判断的考试"
      ></el-input>

      <div class="example-chips">
        <el-tag
          v-for="ex in examples"
          :key="ex"
          class="example-tag"
          @click="agentInput = ex"
        >{{ ex }}</el-tag>
      </div>

      <div style="margin-top: 15px;">
        <el-button type="primary" @click="generateByAgent" :loading="agentGenerating">
          {{ agentGenerating ? 'Agent 生成中...' : 'Agent 生成试卷' }}
        </el-button>
      </div>

      <!-- Agent 执行过程 -->
      <el-steps
        v-if="agentSteps.length"
        :active="agentStepActive"
        finish-status="success"
        align-center
        style="margin-top: 25px;"
      >
        <el-step
          v-for="(step, idx) in agentSteps"
          :key="idx"
          :title="step.name"
          :description="formatStepDetail(step)"
        ></el-step>
      </el-steps>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>PDF 教材出题（高级）</span>
      </div>

      <el-form :model="form" label-width="120px">
        <el-form-item label="上传教材 PDF">
          <el-upload
            class="upload-demo"
            drag
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".pdf"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将 PDF 文件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip" slot="tip">只能上传 PDF 文件</div>
          </el-upload>
        </el-form-item>

        <el-form-item label="学科">
          <el-select v-model="form.subjectId" placeholder="请选择学科">
            <el-option
              v-for="item in subjects"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="年级">
          <el-select v-model="form.gradeLevel" placeholder="请选择年级" clearable>
            <el-option label="大一" :value="1"></el-option>
            <el-option label="大二" :value="2"></el-option>
            <el-option label="大三" :value="3"></el-option>
            <el-option label="大四" :value="4"></el-option>
            <el-option label="研一" :value="5"></el-option>
            <el-option label="研二" :value="6"></el-option>
            <el-option label="研三" :value="7"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="题目类型">
          <el-select v-model="form.questionType" placeholder="请选择题目类型">
            <el-option label="单选题" :value="1"></el-option>
            <el-option label="多选题" :value="2"></el-option>
            <el-option label="判断题" :value="3"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="题目数量">
          <el-input-number v-model="form.questionCount" :min="1" :max="50" label="题目数量"></el-input-number>
        </el-form-item>

        <el-form-item label="难度级别">
          <el-select v-model="form.difficulty" placeholder="请选择难度">
            <el-option label="简单" :value="1"></el-option>
            <el-option label="中等" :value="2"></el-option>
            <el-option label="困难" :value="3"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="生成模式">
          <el-radio-group v-model="form.enableQualityCheck">
            <el-radio :label="true">标准模式（质量评估）</el-radio>
            <el-radio :label="false">快速模式（跳过评估）</el-radio>
          </el-radio-group>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            标准模式会对每道题进行质量评估，速度较慢但质量更高；快速模式跳过评估，速度更快
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="generateQuestions" :loading="generating">
            {{ generating ? '生成中...' : '开始生成题目' }}
          </el-button>
          <el-button type="warning" plain @click="loadDemo">
            加载 Demo 示例（无需 PDF）
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="box-card" v-if="result" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>生成结果</span>
        <el-button style="float: right; padding: 3px 10px" type="success" @click="saveQualifiedQuestions" :loading="saving">
          保存合格题目到题库
        </el-button>
      </div>

      <el-alert
        v-if="demoMode"
        title="当前展示的是内置 Demo 数据（Java Spring Boot 初级开发工程师考试），可直接点击「保存合格题目到题库」体验完整流程。"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 15px;"
      ></el-alert>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总题目数" :value="result.totalCount"></el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="合格题目数" :value="result.qualifiedCount" :value-style="{ color: '#67C23A' }"></el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="不合格题目数" :value="result.rejectedCount" :value-style="{ color: '#F56C6C' }"></el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="处理时间" :value="result.processingTime" suffix="ms"></el-statistic>
        </el-col>
      </el-row>

      <el-tabs v-model="activeTab" style="margin-top: 20px;">
        <el-tab-pane label="合格题目" name="qualified">
          <el-table :data="result.qualifiedQuestions" style="width: 100%" border>
            <el-table-column type="index" label="序号" width="60"></el-table-column>
            <el-table-column prop="question" label="题目" min-width="200"></el-table-column>
            <el-table-column label="选项" min-width="200">
              <template slot-scope="scope">
                <div v-for="(option, index) in scope.row.options" :key="index">
                  {{ option }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="correct_answer" label="正确答案" width="100"></el-table-column>
            <el-table-column prop="qualityScore" label="质量评分" width="100">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.qualityScore != null" :type="scope.row.qualityScore >= 4.5 ? 'success' : scope.row.qualityScore >= 4.0 ? 'warning' : 'danger'">
                  {{ scope.row.qualityScore.toFixed(1) }}
                </el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column prop="analysis" label="解析" min-width="200"></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="不合格题目" name="rejected">
          <el-table :data="result.rejectedQuestions" style="width: 100%" border>
            <el-table-column type="index" label="序号" width="60"></el-table-column>
            <el-table-column prop="question" label="题目" min-width="200"></el-table-column>
            <el-table-column label="选项" min-width="200">
              <template slot-scope="scope">
                <div v-for="(option, index) in scope.row.options" :key="index">
                  {{ option }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="correct_answer" label="正确答案" width="100"></el-table-column>
            <el-table-column prop="qualityScore" label="质量评分" width="100">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.qualityScore != null" type="danger">{{ scope.row.qualityScore.toFixed(1) }}</el-tag>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column prop="analysis" label="解析" min-width="200"></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Markdown 内容" name="markdown">
          <pre class="markdown-content">{{ result.markdownContent }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import subjectApi from '@/api/subject'
import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockAdminApi } from '@/mock/mock-api'
import { buildDemoResult, DEMO_INPUT } from './demo-data'

export default {
  name: 'AiQuestion',
  data () {
    return {
      agentInput: '',
      form: {
        subjectId: null,
        gradeLevel: null,
        questionType: 1,
        questionCount: 10,
        difficulty: 2,
        enableQualityCheck: true
      },
      subjects: [],
      file: null,
      generating: false,
      saving: false,
      result: null,
      activeTab: 'qualified',
      demoMode: false,
      // AI Agent 自然语言出题
      agentGenerating: false,
      agentSteps: [],
      agentStepActive: 0,
      examples: [
        '生成一套 Java 基础，中等难度，20 道题，包含单选/多选/判断的考试',
        '帮我出一套 Python 高级考试，15 道题',
        'MySQL 基础，简单难度，10 道单选题'
      ]
    }
  },
  created() {
    this.fetchSubjects()
  },
  methods: {
    formatStepDetail(step) {
      let detail = step.detail || ''
      if (detail.length > 100) {
        detail = detail.substring(0, 100) + '...'
      }
      return detail + (step.costMs != null ? ' (' + step.costMs + 'ms)' : '')
    },
    fetchSubjects() {
      subjectApi.list().then(response => {
        this.subjects = response.response
      })
    },
    handleFileChange(file) {
      this.file = file.raw
    },
    loadDemo() {
      this.result = buildDemoResult()
      this.demoMode = true
      this.activeTab = 'qualified'
      // Demo 为混合题型，自动选中 Java 学科（无则使用第一个学科）以便保存
      const javaSubject = this.subjects.find(item => item.name.indexOf('Java') !== -1)
      const target = javaSubject || this.subjects[0]
      if (target) {
        this.form.subjectId = target.id
      }
      this.$message.success('已加载 Demo 示例：' + DEMO_INPUT)
    },
    generateByAgent() {
      if (!this.agentInput.trim()) {
        this.$message.error('请输入出题需求')
        return
      }
      this.agentGenerating = true
      this.result = null
      this.demoMode = true
      this.agentSteps = []
      this.agentStepActive = 0

      const apiCall = DEMO_MODE
        ? mockAdminApi.aiGenerateByText(this.agentInput)
        : post('/api/admin/ai-question/generate-by-text', { input: this.agentInput }, 180000)

      apiCall
        .then(response => {
          const data = response.response
          this.result = data
          this.agentSteps = data.steps || []
          this.agentStepActive = this.agentSteps.length
          this.activeTab = 'qualified'
          const subject = data.requirement && data.requirement.subject
          const target = this.subjects.find(item => item.name.indexOf(subject) !== -1) || this.subjects[0]
          if (target) this.form.subjectId = target.id
          this.$message.success(`Agent 已生成 ${data.qualifiedCount} 道题目`)
        })
        .catch(error => {
          this.agentSteps = []
          this.$message.error('Agent 出题失败：' + (error.response?.data?.message || error.message))
        })
        .finally(() => {
          this.agentGenerating = false
        })
    },
    generateQuestions() {
      if (!this.file) {
        this.$message.error('请上传 PDF 文件')
        return
      }

      this.generating = true
      const formData = new FormData()
      formData.append('file', this.file)
      if (this.form.subjectId) formData.append('subjectId', this.form.subjectId)
      if (this.form.gradeLevel) formData.append('gradeLevel', this.form.gradeLevel)
      formData.append('questionType', this.form.questionType)
      formData.append('questionCount', this.form.questionCount)
      formData.append('difficulty', this.form.difficulty)
      formData.append('enableQualityCheck', this.form.enableQualityCheck)

      const query = {
        baseURL: process.env.VUE_APP_URL,
        url: '/api/admin/ai-question/generate',
        method: 'post',
        withCredentials: true,
        timeout: 300000,
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data', 'request-ajax': true }
      }

      this.$http.request(query).then(response => {
        this.result = response.data.response
        this.demoMode = false
        this.generating = false
        this.$message.success('题目生成完成')
      }).catch(error => {
        this.generating = false
        const errorMsg = (error && error.message) ? error.message : (typeof error === 'string' ? error : '未知错误')
        this.$message.error('题目生成失败：' + errorMsg)
      })
    },
    saveQualifiedQuestions() {
      if (!this.result || !this.result.qualifiedQuestions || this.result.qualifiedQuestions.length === 0) {
        this.$message.warning('没有可保存的合格题目')
        return
      }

      if (!this.form.subjectId) {
        this.$message.warning('请先选择学科')
        return
      }

      this.$confirm(`确定要保存 ${this.result.qualifiedCount} 道合格题目到题库吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.saving = true
        const requestData = {
          questions: this.result.qualifiedQuestions,
          subjectId: this.form.subjectId,
          gradeLevel: this.form.gradeLevel,
          // Demo 为混合题型：全局类型传 null，由后端读取每题自带的 questionType
          questionType: this.demoMode ? null : this.form.questionType
        }

        post('/api/admin/ai-question/save', requestData).then(response => {
          this.saving = false
          const savedCount = response.response.length
          this.$message.success(`成功保存 ${savedCount} 道题目到题库`)
          
          this.$confirm('是否跳转到题目列表查看？', '提示', {
            confirmButtonText: '查看题目',
            cancelButtonText: '继续出题',
            type: 'info'
          }).then(() => {
            this.$router.push({
              path: '/exam/question/list'
            })
          }).catch(() => {})
        }).catch(error => {
          this.saving = false
          this.$message.error('保存失败：' + (error.response?.data?.message || error.message))
        })
      }).catch(() => {})
    },
    resetForm() {
      this.form = {
        subjectId: null,
        gradeLevel: null,
        questionType: 1,
        questionCount: 10,
        difficulty: 2,
        enableQualityCheck: true
      }
      this.file = null
      this.result = null
      this.demoMode = false
    }
  }
}
</script>

<style scoped>
.ai-question-container {
  padding: 20px;
}

.markdown-content {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 500px;
  overflow-y: auto;
}

.example-chips {
  margin-top: 10px;
}

.example-tag {
  margin-right: 8px;
  margin-bottom: 6px;
  cursor: pointer;
}

.agent-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}
</style>
