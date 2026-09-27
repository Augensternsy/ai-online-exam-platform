import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockAdminApi } from '@/mock/mock-api'

export default {
  list: query => DEMO_MODE ? mockAdminApi.subjectList() : post('/api/admin/education/subject/list'),
  pageList: query => DEMO_MODE ? mockAdminApi.subjectPageList(query) : post('/api/admin/education/subject/page', query),
  edit: query => post('/api/admin/education/subject/edit', query),
  select: id => post('/api/admin/education/subject/select/' + id),
  deleteSubject: id => post('/api/admin/education/subject/delete/' + id)
}
