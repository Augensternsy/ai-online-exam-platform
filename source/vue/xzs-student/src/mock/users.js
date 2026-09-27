/**
 * Mock 用户数据（学生端）
 */

export const mockUsers = [
  {
    id: 1,
    userName: 'admin',
    realName: '系统管理员',
    role: 3,
    imagePath: null
  },
  {
    id: 3,
    userName: 'Natasha',
    realName: '娜塔莎',
    role: 1,
    imagePath: null
  }
]

export const mockLogin = (userName, password) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      if (userName === 'admin' && password === '123456') {
        resolve({
          code: 1,
          response: {
            id: 1,
            userName: 'admin',
            realName: '系统管理员',
            role: 3,
            imagePath: null
          },
          message: '登录成功'
        })
      } else if (userName === 'Natasha' && password === '123456') {
        resolve({
          code: 1,
          response: {
            id: 3,
            userName: 'Natasha',
            realName: '娜塔莎',
            role: 1,
            imagePath: null
          },
          message: '登录成功'
        })
      } else {
        resolve({
          code: 2,
          message: '用户名或密码错误'
        })
      }
    }, 500)
  })
}

export const mockLogout = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ code: 1, message: '退出成功' })
    }, 300)
  })
}

export const mockGetCurrentUser = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 1,
        response: {
          id: 3,
          userName: 'Natasha',
          realName: '娜塔莎',
          role: 1,
          imagePath: null,
          phone: '13800000003',
          age: 20,
          sex: 2,
          birthDay: '2004-03-20',
          createTime: '2024-03-01 00:00:00'
        }
      })
    }, 300)
  })
}
