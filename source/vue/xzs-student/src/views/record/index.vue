<template>
  <div style="margin-top: 10px" class="app-contain">
     <el-row :gutter="50">
       <el-col :span="18">
         <el-table v-loading="listLoading" :data="tableData" fit highlight-current-row style="width: 100%" @row-click="itemSelect">
           <el-table-column prop="id" label="序号" width="90px"/>
           <el-table-column prop="paperName" label="名称"  />
           <el-table-column prop="subjectName" label="学科"  width="70" />
           <el-table-column label="状态" prop="status" width="100px">
             <template slot-scope="{row}">
               <el-tag :type="statusTagFormatter(row.status)">
                 {{ statusTextFormatter(row.status) }}
               </el-tag>
             </template>
           </el-table-column>
           <el-table-column prop="createTime" label="做题时间"  width="170" />
           <el-table-column  align="right" width="70">
             <template slot-scope="{row}">
               <router-link target="_blank" :to="{path:'/edit',query:{id:row.id}}" v-if="row.status === 1 ">
                 <el-button  type="text" size="small">批改</el-button>
               </router-link>
               <router-link target="_blank" :to="{path:'/read',query:{id:row.id}}" v-if="row.status === 2 ">
                 <el-button  type="text" size="small">查看试卷</el-button>
               </router-link>
             </template>
           </el-table-column>
         </el-table>
         <pagination v-show="total>0" :total="total" :background="false" :page.sync="queryParam.pageIndex" :limit.sync="queryParam.pageSize"
                     @pagination="search" style="margin-top: 20px"/>
       </el-col>
       <el-col  :span="6" >
         <el-card  class="record-answer-info">
            <el-form label-width="50%" >
              <el-form-item label="系统判分：">
                <span>{{selectItem.systemScore}}</span>
              </el-form-item>
              <el-form-item label="最终得分：">
                <span>{{selectItem.userScore}}</span>
              </el-form-item>
              <el-form-item label="试卷总分：">
                <span>{{selectItem.paperScore}}</span>
              </el-form-item>
              <el-form-item label="正确题数：">
                <span>{{selectItem.questionCorrect}}</span>
              </el-form-item>
              <el-form-item label="总题数：">
                <span>{{selectItem.questionCount}}</span>
              </el-form-item>
              <el-form-item label="用时：">
                <span>{{selectItem.doTime}}</span>
              </el-form-item>
            </el-form>

            <el-button
              v-if="selectItem.id && selectItem.status === 2"
              type="primary"
              size="small"
              style="width: 100%; margin-top: 10px;"
              @click="evaluate"
              :loading="evaluating"
            >AI 智能评测</el-button>
         </el-card>

         <!-- AI 评测结果 -->
         <el-card v-if="evaluation" class="eval-card" style="margin-top: 15px;">
           <div slot="header"><b>AI 智能分析报告</b></div>

           <div class="score-row">
             <div class="score-circle" :style="{ background: scoreColor }">
               <span>{{ evaluation.correctRate }}%</span>
               <small>正确率</small>
             </div>
             <div class="score-meta">
               <div>得分：<b>{{ evaluation.score }}</b> / {{ evaluation.paperScore }}</div>
               <div>正确：{{ evaluation.correctCount }} / {{ evaluation.totalCount }}</div>
               <div>错误：{{ evaluation.wrongCount }} 题</div>
             </div>
           </div>

           <!-- 各题型正确率 -->
           <div v-if="evaluation.knowledgeStats && evaluation.knowledgeStats.length" style="margin-top: 15px;">
             <div class="sub-title">各题型正确率</div>
             <div v-for="s in evaluation.knowledgeStats" :key="s.type" class="stat-row">
               <span class="stat-name">{{ s.typeName }}</span>
               <el-progress :percentage="s.rate" :stroke-width="10" :show-text="false"></el-progress>
               <span class="stat-val">{{ s.correct }}/{{ s.total }}</span>
             </div>
           </div>

           <!-- 薄弱知识点 -->
           <div v-if="evaluation.weakPoints && evaluation.weakPoints.length" style="margin-top: 15px;">
             <div class="sub-title">薄弱知识点</div>
             <div>
               <el-tag
                 v-for="(wp, i) in evaluation.weakPoints"
                 :key="i"
                 type="danger"
                 style="margin: 3px;"
               >{{ wp }}</el-tag>
             </div>
           </div>

           <!-- 学习建议 -->
           <div v-if="evaluation.suggestion" style="margin-top: 15px;">
             <div class="sub-title">学习建议</div>
             <div class="suggestion">{{ evaluation.suggestion }}</div>
           </div>
         </el-card>
       </el-col>
     </el-row>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import Pagination from '@/components/Pagination'
import examPaperAnswerApi from '@/api/examPaperAnswer'
import { get } from '@/utils/request'
import { scrollTo } from '@/utils/scroll-to'
export default {
  components: { Pagination },
  data () {
    return {
      queryParam: {
        pageIndex: 1,
        pageSize: 10
      },
      listLoading: false,
      tableData: [],
      total: 0,
      selectItem: {
        id: null,
        systemScore: '0',
        userScore: '0',
        doTime: '0',
        paperScore: '0',
        questionCorrect: 0,
        questionCount: 0,
        status: null
      },
      evaluating: false,
      evaluation: null
    }
  },
  computed: {
    scoreColor () {
      if (!this.evaluation) return '#67C23A'
      const r = this.evaluation.correctRate
      if (r >= 80) return '#67C23A'
      if (r >= 60) return '#E6A23C'
      return '#F56C6C'
    },
    ...mapGetters('enumItem', [
      'enumFormat'
    ]),
    ...mapState('enumItem', {
      statusEnum: state => state.exam.examPaperAnswer.statusEnum,
      statusTag: state => state.exam.examPaperAnswer.statusTag
    })
  },
  created () {
    this.search()
    scrollTo(0, 800)
  },
  methods: {
    search () {
      this.listLoading = true
      let _this = this
      examPaperAnswerApi.pageList(this.queryParam).then(data => {
        const re = data.response
        _this.tableData = re.list
        _this.total = re.total
        _this.queryParam.pageIndex = re.pageNum
        _this.listLoading = false
      })
    },
    itemSelect (row) {
      this.selectItem = row
      this.evaluation = null
    },
    evaluate () {
      if (!this.selectItem.id) return
      this.evaluating = true
      this.evaluation = null
      get(`/api/student/exampaper/answer/evaluate/${this.selectItem.id}`).then(res => {
        this.evaluation = res.response
      }).catch(e => {
        this.$message.error('AI 评测失败：' + (e || '未知错误'))
      }).finally(() => {
        this.evaluating = false
      })
    },
    statusTagFormatter (status) {
      return this.enumFormat(this.statusTag, status)
    },
    statusTextFormatter (status) {
      return this.enumFormat(this.statusEnum, status)
    }
  }
}
</script>

<style lang="scss" scoped>
.eval-card {
  .score-row {
    display: flex;
    align-items: center;
  }
  .score-circle {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    flex-shrink: 0;
    span { font-size: 20px; font-weight: bold; line-height: 1; }
    small { font-size: 11px; margin-top: 4px; opacity: 0.9; }
  }
  .score-meta { font-size: 13px; line-height: 1.8; }
  .sub-title {
    font-size: 13px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 8px;
    padding-left: 6px;
    border-left: 3px solid #409EFF;
  }
  .stat-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    .stat-name { width: 55px; font-size: 12px; color: #606266; }
    .stat-val { width: 50px; text-align: right; font-size: 12px; color: #909399; }
    .el-progress { flex: 1; margin: 0 8px; }
  }
  .suggestion {
    background: #f4f4f5;
    padding: 10px;
    border-radius: 4px;
    font-size: 13px;
    line-height: 1.6;
    color: #303133;
  }
}
</style>
