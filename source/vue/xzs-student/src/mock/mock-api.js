/**
 * Mock API 统一入口（学生端）
 */

import DEMO_MODE from './index'
import { mockLogin, mockLogout, mockGetCurrentUser } from './users'
import { mockExamPaperPageList, mockSelectExamPaper, mockSubjectListRequest } from './exam'
import { mockAnswerSubmit, mockExamPaperAnswerPageList, mockExamPaperAnswerRead, mockSubmitExam } from './answer'
import { mockDashboardIndex, mockDashboardTask } from './dashboard'

/**
 * 学生端 Mock API
 */
export const mockStudentApi = {
  // 登录
  login: (query) => mockLogin(query.userName, query.password),
  logout: () => mockLogout(),
  getCurrentUser: () => mockGetCurrentUser(),

  // 学科
  subjectList: () => mockSubjectListRequest(),

  // 试卷
  examPaperPageList: (query) => mockExamPaperPageList(query),
  selectExamPaper: (id) => mockSelectExamPaper(id),

  // 答题
  startExam: (form) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ code: 1, response: { id: 1, examPaperId: form.examPaperId } })
      }, 300)
    })
  },

  saveAnswer: (form) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ code: 1, message: '答案保存成功' })
      }, 300)
    })
  },

  submitExam: (form) => mockSubmitExam(form),

  answerSubmit: (form) => mockAnswerSubmit(form),

  // 成绩
  examPaperAnswerPageList: (query) => mockExamPaperAnswerPageList(query),
  examPaperAnswerRead: (id) => mockExamPaperAnswerRead(id),

  // 仪表盘
  dashboardIndex: () => mockDashboardIndex(),
  dashboardTask: () => mockDashboardTask()
}

export { DEMO_MODE }
