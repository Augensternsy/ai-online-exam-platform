import { post, postWithLoadTip } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockStudentApi } from '@/mock/mock-api'

export default {
  login: query => DEMO_MODE ? mockStudentApi.login(query) : postWithLoadTip(`/api/user/login`, query),
  logout: query => DEMO_MODE ? mockStudentApi.logout() : post(`/api/user/logout`, query)
}
