/**
 * 浏览器标题统一工具
 * 规则：有页面标题时显示「页面名称 - AI 智能在线考试平台」，否则仅显示系统名称
 */
export const SYSTEM_NAME = 'AI 智能在线考试平台'

export function setDocumentTitle (pageTitle) {
  document.title = pageTitle ? `${pageTitle} - ${SYSTEM_NAME}` : SYSTEM_NAME
}
