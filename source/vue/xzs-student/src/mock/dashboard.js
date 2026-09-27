/**
 * Mock 仪表盘数据（学生端）
 */

export const mockDashboardIndex = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 1,
        response: {
          examPaperCount: 2,
          questionCount: 20,
          doExamPaperCount: 3,
          doQuestionCount: 45,
          examPaperAnswerCount: 3,
          studentCount: 1
        }
      })
    }, 300)
  })
}

export const mockDashboardTask = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 1,
        response: []
      })
    }, 300)
  })
}
