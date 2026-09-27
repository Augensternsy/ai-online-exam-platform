import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockAdminApi } from '@/mock/mock-api'

export default {
  index: () => DEMO_MODE ? mockAdminApi.dashboardIndex() : post('/api/admin/dashboard/index')
}
