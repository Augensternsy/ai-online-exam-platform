import { post, postWithLoadTip } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockAdminApi } from '@/mock/mock-api'

export default {
  login: query => DEMO_MODE ? mockAdminApi.login(query) : postWithLoadTip(`/api/user/login`, query),
  logout: query => DEMO_MODE ? mockAdminApi.logout() : post(`/api/user/logout`, query)
}
