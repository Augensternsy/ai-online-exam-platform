import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockStudentApi } from '@/mock/mock-api'

export default {
  list: query => DEMO_MODE ? mockStudentApi.subjectList() : post('/api/student/education/subject/list'),
  select: id => DEMO_MODE ? mockStudentApi.subjectList() : post('/api/student/education/subject/select/' + id)
}
