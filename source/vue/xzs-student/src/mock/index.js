/**
 * Demo 模式开关
 * 当 VUE_APP_DEMO_MODE=true 时，所有 API 请求使用本地 Mock 数据
 */
const DEMO_MODE = process.env.VUE_APP_DEMO_MODE === 'true'

export default DEMO_MODE
