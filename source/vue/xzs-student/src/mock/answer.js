/**
 * Mock 答题和成绩数据（学生端）
 *
 * 与后端 /api/student/exampaper/answer/ 接口返回结构保持一致
 */

import { mockExamPaper, mockExamQuestions } from './exam'

export const mockAnswerSubmit = (form) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ code: 1, message: '答题提交成功' })
    }, 500)
  })
}

/**
 * 根据提交的答案计算得分并生成判分结果
 * @param {Object} form 提交的表单：{ id, doTime, answerItems }
 */
const gradeExam = (form) => {
  const answerItems = (form && form.answerItems) || []
  let score = 0
  let correctCount = 0

  const gradedItems = answerItems.map((item, idx) => {
    const question = mockExamQuestions.find(q => q.id === item.questionId)
    if (!question) {
      return {
        ...item,
        itemOrder: item.itemOrder || idx + 1,
        doRight: false,
        score: '0'
      }
    }

    let doRight = false
    if (question.questionType === 1 || question.questionType === 3) {
      // 单选 / 判断：content 与 correct 完全一致
      doRight = item.content === question.correct
    } else if (question.questionType === 2) {
      // 多选：contentArray 与 correctArray 完全匹配（顺序无关）
      const userArr = (item.contentArray || []).slice().sort()
      const correctArr = (question.correctArray || question.correct.split(',')).slice().sort()
      doRight = userArr.length === correctArr.length &&
        userArr.every((v, i) => v === correctArr[i])
    }

    if (doRight) {
      score += parseInt(question.score) || 0
      correctCount += 1
    }

    return {
      questionId: item.questionId,
      content: item.content,
      contentArray: item.contentArray,
      completed: !!item.completed,
      itemOrder: item.itemOrder || question.itemOrder,
      doRight: doRight,
      score: doRight ? question.score : '0'
    }
  })

  return { score, correctCount, gradedItems }
}

export const mockSubmitExam = (form) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const { score } = gradeExam(form)
      resolve({
        code: 1,
        message: '提交成功',
        response: score
      })
    }, 500)
  })
}

export const mockExamPaperAnswerPageList = (query) => {
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
              systemScore: '85',
              userScore: '85',
              paperScore: '100',
              questionCorrect: 17,
              questionCount: 20,
              doTime: 1500,
              createTime: '2026-09-27 10:30:00',
              status: 2
            }
          ],
          total: 1,
          pageNum: query && query.pageIndex ? query.pageIndex : 1,
          pageIndex: query && query.pageIndex ? query.pageIndex : 1,
          pageSize: query && query.pageSize ? query.pageSize : 10,
          pages: 1
        }
      })
    }, 300)
  })
}

/**
 * 查看试卷（成绩详情）
 * 返回结构：{ paper: 试卷结构(含 titleItems), answer: 答题结果(含 answerItems 判分) }
 */
export const mockExamPaperAnswerRead = (id) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      // 构造判过分的 answerItems（模拟 17 题正确、3 题错误，得分 85）
      const answerItems = mockExamQuestions.map((q, idx) => {
        // 让第 3、13、16 题答错（itemOrder: 3, 13, 16），其余答对
        const wrongOrders = [3, 13, 16]
        const doRight = !wrongOrders.includes(q.itemOrder)
        let content = null
        let contentArray = []

        if (q.questionType === 1 || q.questionType === 3) {
          content = doRight ? q.correct : (q.correct === 'A' ? 'B' : 'A')
        } else if (q.questionType === 2) {
          contentArray = doRight ? (q.correctArray || q.correct.split(',')) : ['A']
        }

        return {
          questionId: q.id,
          content: content,
          contentArray: contentArray,
          completed: true,
          itemOrder: q.itemOrder,
          doRight: doRight,
          score: doRight ? q.score : '0'
        }
      })

      resolve({
        code: 1,
        response: {
          paper: {
            id: mockExamPaper.id,
            name: mockExamPaper.name,
            score: mockExamPaper.score,
            suggestTime: 30,
            titleItems: [
              { name: '单选题', questionItems: mockExamQuestions.filter(q => q.questionType === 1) },
              { name: '多选题', questionItems: mockExamQuestions.filter(q => q.questionType === 2) },
              { name: '判断题', questionItems: mockExamQuestions.filter(q => q.questionType === 3) }
            ]
          },
          answer: {
            id: Number(id) || 1,
            score: '85',
            doTime: 1500,
            answerItems: answerItems
          }
        }
      })
    }, 300)
  })
}
