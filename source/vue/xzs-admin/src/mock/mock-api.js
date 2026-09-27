/**
 * Mock API 统一入口
 * 当 DEMO_MODE=true 时，替换所有真实 API 调用
 */

import DEMO_MODE from './index'
import { mockLogin, mockLogout, mockGetCurrentUser, mockGetUserPageList } from './users'
import { mockQuestionPageList, mockSelectQuestion } from './questions'
import { mockPaperPageList, mockSelectPaper } from './papers'
import { mockAiGenerate, mockAiGenerateByText } from './ai-demo'
import { mockDashboardIndex } from './dashboard'
import { mockSubjectList, mockSubjectPageList } from './subjects'

/**
 * 管理端 Mock API
 */
export const mockAdminApi = {
  // 登录
  login: (query) => mockLogin(query.userName, query.password),
  logout: () => mockLogout(),
  getCurrentUser: () => mockGetCurrentUser(),

  // 用户管理
  getUserPageList: (query) => mockGetUserPageList(query),

  // 题目管理
  questionPageList: (query) => mockQuestionPageList(query),
  selectQuestion: (id) => mockSelectQuestion(id),

  // 试卷管理
  examPaperPageList: (query) => mockPaperPageList(query),
  selectPaper: (id) => mockSelectPaper(id),

  // AI 出题
  aiGenerate: (input) => mockAiGenerate(input),
  aiGenerateByText: (input) => mockAiGenerateByText(input),

  // 仪表盘
  dashboardIndex: () => mockDashboardIndex(),

  // 学科
  subjectList: () => mockSubjectList(),
  subjectPageList: (query) => mockSubjectPageList(query)
}

/**
 * 学生端 Mock API
 */
export const mockStudentApi = {
  login: (query) => mockLogin(query.userName, query.password),
  logout: () => mockLogout(),
  getCurrentUser: () => mockGetCurrentUser(),

  // 试卷列表
  examPaperPageList: (query) => mockPaperPageList(query),
  selectPaper: (id) => mockSelectPaper(id),

  // 答题
  startExam: (form) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ code: 1, response: { id: 1, examPaperId: form.paperId } })
      }, 300)
    })
  },

  submitExam: (form) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 1,
          response: {
            id: 1,
            score: 85,
            correctCount: 17,
            totalCount: 20
          }
        })
      }, 500)
    })
  },

  // 成绩
  examPaperAnswerPageList: (query) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 1,
          response: {
            list: [
              {
                id: 1,
                paperName: 'Java 基础能力测试',
                paperType: 1,
                subjectName: 'Java 开发技术',
                systemScore: 85,
                userScore: 85,
                paperScore: 100,
                questionCorrect: 17,
                questionCount: 20,
                doTime: 25,
                createTime: '2026-09-27 10:30:00',
                status: 2
              }
            ],
            total: 1,
            pageIndex: query?.pageIndex || 1,
            pageSize: query?.pageSize || 10
          }
        })
      }, 300)
    })
  },

  // 仪表盘
  dashboardIndex: () => mockDashboardIndex()
}

export { DEMO_MODE }
