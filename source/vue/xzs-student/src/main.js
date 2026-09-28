import Vue from 'vue'
import App from './App.vue'
import { router } from './router'
import store from './store'
import 'normalize.css/normalize.css'
import Element from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

import '@/styles/index.scss' // global css
import './icons' // icon
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import '@/assets/background.jpg'
import { setDocumentTitle } from '@/utils/title'

Vue.use(Element, {
  size: 'medium' // set element-ui default size
})

Vue.config.productionTip = false

NProgress.configure({ showSpinner: false }) // NProgress Configuration

router.beforeEach(async (to, from, next) => {
  // start progress bar
  NProgress.start()
  // 统一浏览器标题：页面名称 - AI 智能在线考试平台（无 meta.title 时仅显示系统名称）
  setDocumentTitle(to.meta.title)

  if (to.meta.bodyBackground !== undefined) {
    document.querySelector('body').setAttribute('style', 'background: ' + to.meta.bodyBackground + ';background-size: 100% 100%')
  } else {
    document.querySelector('body').removeAttribute('style')
  }

  if (to.path) {
    // 百度统计未加载时安全跳过
    window._hmt && window._hmt.push(['_trackPageview', '/#' + to.fullPath])
  }
  next()
})

router.afterEach((to, from, next) => {
  // finish progress bar
  NProgress.done()
})

Vue.prototype.$$router = router

new Vue({
  router: router,
  store: store,
  render: h => h(App)
}).$mount('#app')
