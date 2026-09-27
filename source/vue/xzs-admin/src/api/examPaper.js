import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockAdminApi } from '@/mock/mock-api'

export default {
  pageList: query => DEMO_MODE ? mockAdminApi.examPaperPageList(query) : post('/api/admin/exam/paper/page', query),
  taskExamPage: query => post('/api/admin/exam/paper/taskExamPage', query),
  edit: query => post('/api/admin/exam/paper/edit', query),
  select: id => DEMO_MODE ? mockAdminApi.selectPaper(id) : post('/api/admin/exam/paper/select/' + id),
  deletePaper: id => post('/api/admin/exam/paper/delete/' + id)
}
