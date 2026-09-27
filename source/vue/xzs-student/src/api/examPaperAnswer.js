import { post } from '@/utils/request'
import DEMO_MODE from '@/mock'
import { mockStudentApi } from '@/mock/mock-api'

export default {
  pageList: query => DEMO_MODE ? mockStudentApi.examPaperAnswerPageList(query) : post('/api/student/exampaper/answer/pageList', query),
  answerSubmit: form => post('/api/student/exampaper/answer/answerSubmit', form),
  read: id => DEMO_MODE ? mockStudentApi.examPaperAnswerRead(id) : post('/api/student/exampaper/answer/read/' + id),
  edit: form => post('/api/student/exampaper/answer/edit', form),
  startExam: form => DEMO_MODE ? mockStudentApi.startExam(form) : post('/api/student/exampaper/answer/start', form),
  saveAnswer: form => DEMO_MODE ? mockStudentApi.saveAnswer(form) : post('/api/student/exampaper/answer/saveAnswer', form),
  submitExam: form => DEMO_MODE ? mockStudentApi.submitExam(form) : post('/api/student/exampaper/answer/submitExam', form)
}
