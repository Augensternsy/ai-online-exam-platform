import axios from 'axios'
import vue from 'vue'

const request = function (loadtip, query) {
  let loading
  if (loadtip) {
    loading = vue.prototype.$loading({
      lock: false,
      text: '正在加载中…',
      spinner: 'el-icon-loading',
      background: 'rgba(0, 0, 0, 0.5)'
    })
  }
  return axios.request(query)
    .then(res => {
      if (loadtip) {
        loading.close()
      }
      if (res.data.code === 401) {
        vue.prototype.$$router.push({ path: '/login' })
        return Promise.reject(res.data)
      } else if (res.data.code === 500) {
        vue.prototype.$message.error('服务器异常，请稍后重试')
        return Promise.reject(res.data)
      } else if (res.data.code === 501) {
        return Promise.reject(res.data)
      } else if (res.data.code === 502) {
        vue.prototype.$message.error('网关异常，请稍后重试')
        return Promise.reject(res.data)
      } else {
        return Promise.resolve(res.data)
      }
    })
    .catch(e => {
      if (loadtip) {
        loading.close()
      }
      // 区分HTTP错误和业务错误
      if (e.response) {
        // 服务器返回了错误状态码
        const status = e.response.status
        if (status === 500) {
          vue.prototype.$message.error('服务器内部错误（500），请稍后重试')
        } else if (status === 502) {
          vue.prototype.$message.error('网关错误（502），请稍后重试')
        } else if (status === 504) {
          vue.prototype.$message.error('请求超时（504），请检查网络后重试')
        } else {
          vue.prototype.$message.error(`请求失败（${status}）`)
        }
      } else if (e.code === 'ECONNABORTED') {
        vue.prototype.$message.error('请求超时，请检查网络连接')
      } else {
        vue.prototype.$message.error('网络异常，请检查网络连接')
      }
      return Promise.reject(e.message)
    })
}

const post = function (url, params) {
  const query = {
    url: url,
    method: 'post',
    withCredentials: true,
    timeout: 30000,
    data: params,
    headers: { 'Content-Type': 'application/json', 'request-ajax': true }
  }
  return request(false, query)
}

const postWithLoadTip = function (url, params) {
  const query = {
    url: url,
    method: 'post',
    withCredentials: true,
    timeout: 30000,
    data: params,
    headers: { 'Content-Type': 'application/json', 'request-ajax': true }
  }
  return request(true, query)
}

const postWithOutLoadTip = function (url, params) {
  const query = {
    url: url,
    method: 'post',
    withCredentials: true,
    timeout: 30000,
    data: params,
    headers: { 'Content-Type': 'application/json', 'request-ajax': true }
  }
  return request(false, query)
}

const get = function (url, params) {
  const query = {
    url: url,
    method: 'get',
    withCredentials: true,
    timeout: 30000,
    params: params,
    headers: { 'request-ajax': true }
  }
  return request(false, query)
}

const form = function (url, params) {
  const query = {
    url: url,
    method: 'post',
    withCredentials: true,
    timeout: 30000,
    data: params,
    headers: { 'Content-Type': 'multipart/form-data', 'request-ajax': true }
  }
  return request(false, query)
}

export {
  post,
  postWithLoadTip,
  postWithOutLoadTip,
  get,
  form
}
