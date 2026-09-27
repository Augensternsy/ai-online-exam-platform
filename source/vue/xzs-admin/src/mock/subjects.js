/**
 * Mock 学科数据
 */

export const mockSubjects = [
  {
    id: 1,
    name: 'Java 开发技术',
    level: 1,
    levelName: '大学一年级'
  },
  {
    id: 2,
    name: 'Python 基础',
    level: 1,
    levelName: '大学一年级'
  },
  {
    id: 3,
    name: '数据库原理',
    level: 1,
    levelName: '大学一年级'
  }
]

export const mockSubjectList = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 1,
        response: mockSubjects
      })
    }, 300)
  })
}

export const mockSubjectPageList = (query) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const pageIndex = query?.pageIndex || 1
      const pageSize = query?.pageSize || 10
      resolve({
        code: 1,
        response: {
          list: mockSubjects.slice((pageIndex - 1) * pageSize, pageIndex * pageSize),
          total: mockSubjects.length,
          pageIndex,
          pageSize
        }
      })
    }, 300)
  })
}
