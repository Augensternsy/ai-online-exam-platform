/**
 * Mock 用户数据
 */

export const mockUsers = [
  {
    id: 1,
    userName: 'admin',
    realName: '系统管理员',
    role: 3,
    age: 28,
    sex: 1,
    birthDay: '1996-01-01',
    phone: '13800000001',
    lastActiveTime: '2026-09-27 10:00:00',
    createTime: '2024-01-01 00:00:00',
    status: 1,
    imagePath: null
  },
  {
    id: 2,
    userName: 'teacher',
    realName: '张老师',
    role: 3,
    age: 35,
    sex: 2,
    birthDay: '1989-05-15',
    phone: '13800000002',
    lastActiveTime: '2026-09-26 18:30:00',
    createTime: '2024-01-15 00:00:00',
    status: 1,
    imagePath: null
  },
  {
    id: 3,
    userName: 'Natasha',
    realName: '娜塔莎',
    role: 1,
    age: 20,
    sex: 2,
    birthDay: '2004-03-20',
    phone: '13800000003',
    lastActiveTime: '2026-09-27 09:15:00',
    createTime: '2024-03-01 00:00:00',
    status: 1,
    imagePath: null
  },
  {
    id: 4,
    userName: 'student1',
    realName: '学生一号',
    role: 1,
    age: 19,
    sex: 1,
    birthDay: '2005-06-10',
    phone: '13800000004',
    lastActiveTime: '2026-09-25 14:20:00',
    createTime: '2024-03-01 00:00:00',
    status: 1,
    imagePath: null
  },
  {
    id: 5,
    userName: 'student2',
    realName: '学生二号',
    role: 1,
    age: 21,
    sex: 2,
    birthDay: '2003-11-08',
    phone: '13800000005',
    lastActiveTime: '2026-09-24 16:45:00',
    createTime: '2024-03-01 00:00:00',
    status: 1,
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
          id: 1,
          userName: 'admin',
          realName: '系统管理员',
          role: 3,
          imagePath: null,
          phone: '13800000001',
          age: 28,
          sex: 1,
          birthDay: '1996-01-01',
          createTime: '2024-01-01 00:00:00'
        }
      })
    }, 300)
  })
}

export const mockGetUserPageList = (query) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const pageIndex = query?.pageIndex || 1
      const pageSize = query?.pageSize || 10
      const role = query?.role
      const userName = query?.userName || ''

      let filtered = mockUsers.filter(u => {
        if (role && u.role !== role) return false
        if (userName && !u.userName.includes(userName) && !u.realName.includes(userName)) return false
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
