import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockStudentApi } from '@/mock/mock-api'

export default {
  index: () => DEMO_MODE ? mockStudentApi.dashboardIndex() : post('/api/student/dashboard/index'),
  task: () => DEMO_MODE ? mockStudentApi.dashboardTask() : post('/api/student/dashboard/task')
}
