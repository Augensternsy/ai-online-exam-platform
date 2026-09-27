/**
 * Mock 试卷数据
 */

export const mockPapers = [
  {
    id: 1,
    name: 'Java 基础能力测试',
    paperType: 1,
    subjectId: 1,
    gradeLevel: 1,
    score: 100,
    questionCount: 20,
    difficult: 2,
    limitStartTime: null,
    limitEndTime: null,
    createUser: 1,
    createTime: '2024-03-01 10:00:00',
    deleted: false,
    frameTextContentId: 1
  },
  {
    id: 2,
    name: 'Spring Boot 入门测试',
    paperType: 1,
    subjectId: 1,
    gradeLevel: 1,
    score: 100,
    questionCount: 8,
    difficult: 1,
    limitStartTime: null,
    limitEndTime: null,
    createUser: 1,
    createTime: '2024-03-05 14:00:00',
    deleted: false,
    frameTextContentId: 2
  },
  {
    id: 3,
    name: 'Java 集合框架专项',
    paperType: 1,
    subjectId: 1,
    gradeLevel: 1,
    score: 100,
    questionCount: 10,
    difficult: 2,
    limitStartTime: null,
    limitEndTime: null,
    createUser: 1,
    createTime: '2024-03-10 09:00:00',
    deleted: false,
    frameTextContentId: 3
  }
]

export const mockPaperPageList = (query) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const pageIndex = query?.pageIndex || 1
      const pageSize = query?.pageSize || 10
      const name = query?.name || ''

      let filtered = mockPapers.filter(p => {
        if (name && !p.name.includes(name)) return false
        return true
      })

      resolve({
        code: 1,
        response: {
          list: filtered.slice((pageIndex - 1) * pageSize, pageIndex * pageSize),
          total: filtered.length,
          pageIndex,
          pageSize
        }
      })
    }, 300)
  })
}

export const mockSelectPaper = (id) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const p = mockPapers.find(item => item.id === id)
      resolve({
        code: 1,
        response: p || null
      })
    }, 300)
  })
}
