/**
 * Mock 仪表盘数据
 */

export const mockDashboardData = {
  examPaperCount: 3,
  questionCount: 20,
  doExamPaperCount: 15,
  doQuestionCount: 150,
  examPaperAnswerCount: 12,
  studentCount: 5,
  mounthStudentDoQuestionNum: [10, 15, 20, 25, 30, 28, 35, 40, 32, 38, 45, 50],
  mounthStudentCreateNum: [1, 0, 2, 0, 1, 0, 0, 1, 0, 0, 0, 0]
}

export const mockDashboardIndex = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 1,
        response: mockDashboardData
      })
    }, 300)
  })
}
