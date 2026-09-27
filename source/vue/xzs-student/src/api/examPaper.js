import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockStudentApi } from '@/mock/mock-api'

export default {
  select: id => DEMO_MODE ? mockStudentApi.selectExamPaper(id) : post('/api/student/exam/paper/select/' + id),
  pageList: query => DEMO_MODE ? mockStudentApi.examPaperPageList(query) : post('/api/student/exam/paper/pageList', query)
}
