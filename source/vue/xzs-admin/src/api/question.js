import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockAdminApi } from '@/mock/mock-api'

export default {
  pageList: query => DEMO_MODE ? mockAdminApi.questionPageList(query) : post('/api/admin/question/page', query),
  edit: query => post('/api/admin/question/edit', query),
  select: id => DEMO_MODE ? mockAdminApi.selectQuestion(id) : post('/api/admin/question/select/' + id),
  deleteQuestion: id => post('/api/admin/question/delete/' + id)
}
