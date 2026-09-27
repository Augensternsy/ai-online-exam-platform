package com.mindskip.xzs.service.impl;

import com.mindskip.xzs.domain.*;
import com.mindskip.xzs.domain.enums.ExamPaperAnswerStatusEnum;
import com.mindskip.xzs.domain.enums.ExamPaperTypeEnum;
import com.mindskip.xzs.domain.enums.QuestionTypeEnum;
import com.mindskip.xzs.domain.exam.ExamPaperTitleItemObject;
import com.mindskip.xzs.domain.other.KeyValue;
import com.mindskip.xzs.domain.other.ExamPaperAnswerUpdate;
import com.mindskip.xzs.domain.task.TaskItemAnswerObject;
import com.mindskip.xzs.repository.*;
import com.mindskip.xzs.repository.ExamPaperAnswerMapper;
import com.mindskip.xzs.repository.ExamPaperMapper;
import com.mindskip.xzs.repository.QuestionMapper;
import com.mindskip.xzs.repository.TaskExamCustomerAnswerMapper;
import com.mindskip.xzs.service.ExamPaperAnswerService;
import com.mindskip.xzs.service.ExamPaperQuestionCustomerAnswerService;
import com.mindskip.xzs.service.TextContentService;
import com.mindskip.xzs.utility.DateTimeUtil;
import com.mindskip.xzs.utility.ExamUtil;
import com.mindskip.xzs.utility.JsonUtil;
import com.mindskip.xzs.viewmodel.student.exam.ExamAnswerSaveVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperSubmitItemVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperSubmitVM;
import com.mindskip.xzs.viewmodel.student.exampaper.ExamPaperAnswerPageVM;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamPaperAnswerServiceImpl extends BaseServiceImpl<ExamPaperAnswer> implements ExamPaperAnswerService {

    private final ExamPaperAnswerMapper examPaperAnswerMapper;
    private final ExamPaperMapper examPaperMapper;
    private final TextContentService textContentService;
    private final QuestionMapper questionMapper;
    private final ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService;
    private final TaskExamCustomerAnswerMapper taskExamCustomerAnswerMapper;

    @Autowired
    public ExamPaperAnswerServiceImpl(ExamPaperAnswerMapper examPaperAnswerMapper, ExamPaperMapper examPaperMapper, TextContentService textContentService, QuestionMapper questionMapper, ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService, TaskExamCustomerAnswerMapper taskExamCustomerAnswerMapper) {
        super(examPaperAnswerMapper);
        this.examPaperAnswerMapper = examPaperAnswerMapper;
        this.examPaperMapper = examPaperMapper;
        this.textContentService = textContentService;
        this.questionMapper = questionMapper;
        this.examPaperQuestionCustomerAnswerService = examPaperQuestionCustomerAnswerService;
        this.taskExamCustomerAnswerMapper = taskExamCustomerAnswerMapper;
    }

    @Override
    public PageInfo<ExamPaperAnswer> studentPage(ExamPaperAnswerPageVM requestVM) {
        return PageHelper.startPage(requestVM.getPageIndex(), requestVM.getPageSize(), "id desc").doSelectPageInfo(() ->
                examPaperAnswerMapper.studentPage(requestVM));
    }


    @Override
    public ExamPaperAnswerInfo calculateExamPaperAnswer(ExamPaperSubmitVM examPaperSubmitVM, User user) {
        ExamPaperAnswerInfo examPaperAnswerInfo = new ExamPaperAnswerInfo();
        Date now = new Date();
        ExamPaper examPaper = examPaperMapper.selectByPrimaryKey(examPaperSubmitVM.getId());
        ExamPaperTypeEnum paperTypeEnum = ExamPaperTypeEnum.fromCode(examPaper.getPaperType());
        //任务试卷只能做一次
        if (paperTypeEnum == ExamPaperTypeEnum.Task) {
            ExamPaperAnswer examPaperAnswer = examPaperAnswerMapper.getByPidUid(examPaperSubmitVM.getId(), user.getId());
            if (null != examPaperAnswer)
                return null;
        }
        String frameTextContent = textContentService.selectById(examPaper.getFrameTextContentId()).getContent();
        List<ExamPaperTitleItemObject> examPaperTitleItemObjects = JsonUtil.toJsonListObject(frameTextContent, ExamPaperTitleItemObject.class);
        List<Integer> questionIds = examPaperTitleItemObjects.stream().flatMap(t -> t.getQuestionItems().stream().map(q -> q.getId())).collect(Collectors.toList());
        List<Question> questions = questionMapper.selectByIds(questionIds);
        //将题目结构的转化为题目答案
        List<ExamPaperQuestionCustomerAnswer> examPaperQuestionCustomerAnswers = examPaperTitleItemObjects.stream()
                .flatMap(t -> t.getQuestionItems().stream()
                        .map(q -> {
                            Question question = questions.stream().filter(tq -> tq.getId().equals(q.getId())).findFirst().get();
                            ExamPaperSubmitItemVM customerQuestionAnswer = examPaperSubmitVM.getAnswerItems().stream()
                                    .filter(tq -> tq.getQuestionId().equals(q.getId()))
                                    .findFirst()
                                    .orElse(null);
                            return ExamPaperQuestionCustomerAnswerFromVM(question, customerQuestionAnswer, examPaper, q.getItemOrder(), user, now);
                        })
                ).collect(Collectors.toList());

        ExamPaperAnswer examPaperAnswer = ExamPaperAnswerFromVM(examPaperSubmitVM, examPaper, examPaperQuestionCustomerAnswers, user, now);
        examPaperAnswerInfo.setExamPaper(examPaper);
        examPaperAnswerInfo.setExamPaperAnswer(examPaperAnswer);
        examPaperAnswerInfo.setExamPaperQuestionCustomerAnswers(examPaperQuestionCustomerAnswers);
        return examPaperAnswerInfo;
    }

    @Override
    @Transactional
    public String judge(ExamPaperSubmitVM examPaperSubmitVM) {
        ExamPaperAnswer examPaperAnswer = examPaperAnswerMapper.selectByPrimaryKey(examPaperSubmitVM.getId());
        List<ExamPaperSubmitItemVM> judgeItems = examPaperSubmitVM.getAnswerItems().stream().filter(d -> d.getDoRight() == null).collect(Collectors.toList());
        List<ExamPaperAnswerUpdate> examPaperAnswerUpdates = new ArrayList<>(judgeItems.size());
        Integer customerScore = examPaperAnswer.getUserScore();
        Integer questionCorrect = examPaperAnswer.getQuestionCorrect();
        for (ExamPaperSubmitItemVM d : judgeItems) {
            ExamPaperAnswerUpdate examPaperAnswerUpdate = new ExamPaperAnswerUpdate();
            examPaperAnswerUpdate.setId(d.getId());
            examPaperAnswerUpdate.setCustomerScore(ExamUtil.scoreFromVM(d.getScore()));
            boolean doRight = examPaperAnswerUpdate.getCustomerScore().equals(ExamUtil.scoreFromVM(d.getQuestionScore()));
            examPaperAnswerUpdate.setDoRight(doRight);
            examPaperAnswerUpdates.add(examPaperAnswerUpdate);
            customerScore += examPaperAnswerUpdate.getCustomerScore();
            if (examPaperAnswerUpdate.getDoRight()) {
                ++questionCorrect;
            }
        }
        examPaperAnswer.setUserScore(customerScore);
        examPaperAnswer.setQuestionCorrect(questionCorrect);
        examPaperAnswer.setStatus(ExamPaperAnswerStatusEnum.Complete.getCode());
        examPaperAnswerMapper.updateByPrimaryKeySelective(examPaperAnswer);
        examPaperQuestionCustomerAnswerService.updateScore(examPaperAnswerUpdates);

        ExamPaperTypeEnum examPaperTypeEnum = ExamPaperTypeEnum.fromCode(examPaperAnswer.getPaperType());
        switch (examPaperTypeEnum) {
            case Task:
                //任务试卷批改完成后，需要更新任务的状态
                ExamPaper examPaper = examPaperMapper.selectByPrimaryKey(examPaperAnswer.getExamPaperId());
                Integer taskId = examPaper.getTaskExamId();
                Integer userId = examPaperAnswer.getCreateUser();
                TaskExamCustomerAnswer taskExamCustomerAnswer = taskExamCustomerAnswerMapper.getByTUid(taskId, userId);
                TextContent textContent = textContentService.selectById(taskExamCustomerAnswer.getTextContentId());
                List<TaskItemAnswerObject> taskItemAnswerObjects = JsonUtil.toJsonListObject(textContent.getContent(), TaskItemAnswerObject.class);
                taskItemAnswerObjects.stream()
                        .filter(d -> d.getExamPaperAnswerId().equals(examPaperAnswer.getId()))
                        .findFirst().ifPresent(taskItemAnswerObject -> taskItemAnswerObject.setStatus(examPaperAnswer.getStatus()));
                textContentService.jsonConvertUpdate(textContent, taskItemAnswerObjects, null);
                textContentService.updateByIdFilter(textContent);
                break;
            default:
                break;
        }
        return ExamUtil.scoreToVM(customerScore);
    }

    @Override
    public ExamPaperSubmitVM examPaperAnswerToVM(Integer id) {
        ExamPaperSubmitVM examPaperSubmitVM = new ExamPaperSubmitVM();
        ExamPaperAnswer examPaperAnswer = examPaperAnswerMapper.selectByPrimaryKey(id);
        examPaperSubmitVM.setId(examPaperAnswer.getId());
        examPaperSubmitVM.setDoTime(examPaperAnswer.getDoTime());
        examPaperSubmitVM.setScore(ExamUtil.scoreToVM(examPaperAnswer.getUserScore()));
        List<ExamPaperQuestionCustomerAnswer> examPaperQuestionCustomerAnswers = examPaperQuestionCustomerAnswerService.selectListByPaperAnswerId(examPaperAnswer.getId());
        List<ExamPaperSubmitItemVM> examPaperSubmitItemVMS = examPaperQuestionCustomerAnswers.stream()
                .map(a -> examPaperQuestionCustomerAnswerService.examPaperQuestionCustomerAnswerToVM(a))
                .collect(Collectors.toList());
        examPaperSubmitVM.setAnswerItems(examPaperSubmitItemVMS);
        return examPaperSubmitVM;
    }

    @Override
    public Integer selectAllCount() {
        return examPaperAnswerMapper.selectAllCount();
    }

    @Override
    public List<Integer> selectMothCount() {
        Date startTime = DateTimeUtil.getMonthStartDay();
        Date endTime = DateTimeUtil.getMonthEndDay();
        List<KeyValue> mouthCount = examPaperAnswerMapper.selectCountByDate(startTime, endTime);
        List<String> mothStartToNowFormat = DateTimeUtil.MothStartToNowFormat();
        return mothStartToNowFormat.stream().map(md -> {
            KeyValue keyValue = mouthCount.stream().filter(kv -> kv.getName().equals(md)).findAny().orElse(null);
            return null == keyValue ? 0 : keyValue.getValue();
        }).collect(Collectors.toList());
    }


    /**
     * 用户提交答案的转化存储对象
     *
     * @param question               question
     * @param customerQuestionAnswer customerQuestionAnswer
     * @param examPaper              examPaper
     * @param itemOrder              itemOrder
     * @param user                   user
     * @param now                    now
     * @return ExamPaperQuestionCustomerAnswer
     */
    private ExamPaperQuestionCustomerAnswer ExamPaperQuestionCustomerAnswerFromVM(Question question, ExamPaperSubmitItemVM customerQuestionAnswer, ExamPaper examPaper, Integer itemOrder, User user, Date now) {
        ExamPaperQuestionCustomerAnswer examPaperQuestionCustomerAnswer = new ExamPaperQuestionCustomerAnswer();
        examPaperQuestionCustomerAnswer.setQuestionId(question.getId());
        examPaperQuestionCustomerAnswer.setExamPaperId(examPaper.getId());
        examPaperQuestionCustomerAnswer.setQuestionScore(question.getScore());
        examPaperQuestionCustomerAnswer.setSubjectId(examPaper.getSubjectId());
        examPaperQuestionCustomerAnswer.setItemOrder(itemOrder);
        examPaperQuestionCustomerAnswer.setCreateTime(now);
        examPaperQuestionCustomerAnswer.setCreateUser(user.getId());
        examPaperQuestionCustomerAnswer.setQuestionType(question.getQuestionType());
        examPaperQuestionCustomerAnswer.setQuestionTextContentId(question.getInfoTextContentId());
        if (null == customerQuestionAnswer) {
            examPaperQuestionCustomerAnswer.setCustomerScore(0);
        } else {
            setSpecialFromVM(examPaperQuestionCustomerAnswer, question, customerQuestionAnswer);
        }
        return examPaperQuestionCustomerAnswer;
    }

    /**
     * 判断提交答案是否正确，保留用户提交的答案
     *
     * @param examPaperQuestionCustomerAnswer examPaperQuestionCustomerAnswer
     * @param question                        question
     * @param customerQuestionAnswer          customerQuestionAnswer
     */
    private void setSpecialFromVM(ExamPaperQuestionCustomerAnswer examPaperQuestionCustomerAnswer, Question question, ExamPaperSubmitItemVM customerQuestionAnswer) {
        QuestionTypeEnum questionTypeEnum = QuestionTypeEnum.fromCode(examPaperQuestionCustomerAnswer.getQuestionType());
        switch (questionTypeEnum) {
            case SingleChoice:
            case TrueFalse:
                examPaperQuestionCustomerAnswer.setAnswer(customerQuestionAnswer.getContent());
                examPaperQuestionCustomerAnswer.setDoRight(question.getCorrect().equals(customerQuestionAnswer.getContent()));
                examPaperQuestionCustomerAnswer.setCustomerScore(examPaperQuestionCustomerAnswer.getDoRight() ? question.getScore() : 0);
                break;
            case MultipleChoice:
                String customerAnswer = ExamUtil.contentToString(customerQuestionAnswer.getContentArray());
                examPaperQuestionCustomerAnswer.setAnswer(customerAnswer);
                examPaperQuestionCustomerAnswer.setDoRight(customerAnswer.equals(question.getCorrect()));
                examPaperQuestionCustomerAnswer.setCustomerScore(examPaperQuestionCustomerAnswer.getDoRight() ? question.getScore() : 0);
                break;
            case GapFilling:
                String correctAnswer = JsonUtil.toJsonStr(customerQuestionAnswer.getContentArray());
                examPaperQuestionCustomerAnswer.setAnswer(correctAnswer);
                examPaperQuestionCustomerAnswer.setCustomerScore(0);
                break;
            default:
                examPaperQuestionCustomerAnswer.setAnswer(customerQuestionAnswer.getContent());
                examPaperQuestionCustomerAnswer.setCustomerScore(0);
                break;
        }
    }

    private ExamPaperAnswer ExamPaperAnswerFromVM(ExamPaperSubmitVM examPaperSubmitVM, ExamPaper examPaper, List<ExamPaperQuestionCustomerAnswer> examPaperQuestionCustomerAnswers, User user, Date now) {
        Integer systemScore = examPaperQuestionCustomerAnswers.stream().mapToInt(a -> a.getCustomerScore()).sum();
        long questionCorrect = examPaperQuestionCustomerAnswers.stream().filter(a -> a.getCustomerScore().equals(a.getQuestionScore())).count();
        ExamPaperAnswer examPaperAnswer = new ExamPaperAnswer();
        examPaperAnswer.setPaperName(examPaper.getName());
        examPaperAnswer.setDoTime(examPaperSubmitVM.getDoTime());
        examPaperAnswer.setExamPaperId(examPaper.getId());
        examPaperAnswer.setCreateUser(user.getId());
        examPaperAnswer.setCreateTime(now);
        examPaperAnswer.setSubjectId(examPaper.getSubjectId());
        examPaperAnswer.setQuestionCount(examPaper.getQuestionCount());
        examPaperAnswer.setPaperScore(examPaper.getScore());
        examPaperAnswer.setPaperType(examPaper.getPaperType());
        examPaperAnswer.setSystemScore(systemScore);
        examPaperAnswer.setUserScore(systemScore);
        examPaperAnswer.setTaskExamId(examPaper.getTaskExamId());
        examPaperAnswer.setQuestionCorrect((int) questionCorrect);
        boolean needJudge = examPaperQuestionCustomerAnswers.stream().anyMatch(d -> QuestionTypeEnum.needSaveTextContent(d.getQuestionType()));
        if (needJudge) {
            examPaperAnswer.setStatus(ExamPaperAnswerStatusEnum.WaitJudge.getCode());
        } else {
            examPaperAnswer.setStatus(ExamPaperAnswerStatusEnum.Complete.getCode());
        }
        return examPaperAnswer;
    }


    @Override
    public PageInfo<ExamPaperAnswer> adminPage(com.mindskip.xzs.viewmodel.admin.paper.ExamPaperAnswerPageRequestVM requestVM) {
        return PageHelper.startPage(requestVM.getPageIndex(), requestVM.getPageSize(), "id desc").doSelectPageInfo(() ->
                examPaperAnswerMapper.adminPage(requestVM));
    }

    @Override
    @Transactional
    public ExamPaperAnswer startExam(Integer examPaperId, User user) {
        ExamPaperAnswer existing = examPaperAnswerMapper.getByPidUid(examPaperId, user.getId());
        if (existing != null) {
            return existing;
        }

        ExamPaper examPaper = examPaperMapper.selectByPrimaryKey(examPaperId);
        Date now = new Date();

        ExamPaperAnswer examPaperAnswer = new ExamPaperAnswer();
        examPaperAnswer.setPaperName(examPaper.getName());
        examPaperAnswer.setDoTime(0);
        examPaperAnswer.setExamPaperId(examPaperId);
        examPaperAnswer.setCreateUser(user.getId());
        examPaperAnswer.setCreateTime(now);
        examPaperAnswer.setUpdateTime(now);
        examPaperAnswer.setSubjectId(examPaper.getSubjectId());
        examPaperAnswer.setQuestionCount(examPaper.getQuestionCount());
        examPaperAnswer.setPaperScore(examPaper.getScore());
        examPaperAnswer.setPaperType(examPaper.getPaperType());
        examPaperAnswer.setSystemScore(0);
        examPaperAnswer.setUserScore(0);
        examPaperAnswer.setTaskExamId(examPaper.getTaskExamId());
        examPaperAnswer.setQuestionCorrect(0);
        examPaperAnswer.setStatus(ExamPaperAnswerStatusEnum.InProgress.getCode());

        insertByFilter(examPaperAnswer);
        return examPaperAnswer;
    }

    @Override
    @Transactional
    public boolean saveAnswer(ExamAnswerSaveVM saveVM, User user) {
        ExamPaperAnswer examPaperAnswer = examPaperAnswerMapper.getByPidUid(saveVM.getExamPaperId(), user.getId());
        if (examPaperAnswer == null) {
            return false;
        }

        ExamPaperAnswerStatusEnum statusEnum = ExamPaperAnswerStatusEnum.fromCode(examPaperAnswer.getStatus());
        if (statusEnum == ExamPaperAnswerStatusEnum.Complete) {
            return false;
        }

        Date now = new Date();
        examPaperAnswer.setDoTime(saveVM.getDoTime());
        examPaperAnswer.setUpdateTime(now);

        Integer systemScore = 0;
        long questionCorrect = 0;

        List<ExamPaperQuestionCustomerAnswer> existingAnswers = examPaperQuestionCustomerAnswerService.selectListByPaperAnswerId(examPaperAnswer.getId());

        // 批量加载本次试卷的题目信息，供新建答题记录、脏数据自愈和客观题判分使用
        List<Integer> questionIds = saveVM.getAnswerItems().stream()
                .map(ExamPaperSubmitItemVM::getQuestionId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, Question> questionMap = questionIds.isEmpty()
                ? new HashMap<>()
                : questionMapper.selectByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a));

        for (ExamPaperSubmitItemVM itemVM : saveVM.getAnswerItems()) {
            Question question = questionMap.get(itemVM.getQuestionId());
            ExamPaperQuestionCustomerAnswer existingAnswer = existingAnswers.stream()
                    .filter(a -> a.getQuestionId().equals(itemVM.getQuestionId()))
                    .findFirst()
                    .orElse(null);

            if (existingAnswer != null) {
                // 脏数据自愈：早期版本保存时漏设了 questionType/itemOrder/questionScore 等字段
                repairQuestionAnswer(existingAnswer, question, itemVM.getItemOrder());
                // 客观题每次保存按最新答案重新判分，主观题仅更新答案内容
                judgeObjectiveAnswer(existingAnswer, itemVM, question);
                updateQuestionAnswer(existingAnswer, itemVM);
                examPaperQuestionCustomerAnswerService.updateAnswer(existingAnswer);
                systemScore += existingAnswer.getCustomerScore() == null ? 0 : existingAnswer.getCustomerScore();
                if (Boolean.TRUE.equals(existingAnswer.getDoRight())) {
                    questionCorrect++;
                }
            } else {
                ExamPaperQuestionCustomerAnswer newAnswer = createQuestionAnswer(itemVM, question, examPaperAnswer, user, now);
                examPaperQuestionCustomerAnswerService.insertByFilter(newAnswer);
                systemScore += newAnswer.getCustomerScore() == null ? 0 : newAnswer.getCustomerScore();
                if (Boolean.TRUE.equals(newAnswer.getDoRight())) {
                    questionCorrect++;
                }
            }
        }

        examPaperAnswer.setSystemScore(systemScore);
        examPaperAnswer.setUserScore(systemScore);
        examPaperAnswer.setQuestionCorrect((int) questionCorrect);
        examPaperAnswerMapper.updateByPrimaryKeySelective(examPaperAnswer);

        return true;
    }

    @Override
    @Transactional
    public String submitExam(ExamPaperSubmitVM examPaperSubmitVM, User user) {
        ExamPaperAnswer examPaperAnswer = examPaperAnswerMapper.selectByPrimaryKey(examPaperSubmitVM.getId());
        if (examPaperAnswer == null) {
            return null;
        }

        ExamPaperAnswerStatusEnum statusEnum = ExamPaperAnswerStatusEnum.fromCode(examPaperAnswer.getStatus());
        if (statusEnum == ExamPaperAnswerStatusEnum.Complete) {
            return null;
        }

        Date now = new Date();
        examPaperAnswer.setDoTime(examPaperSubmitVM.getDoTime());
        examPaperAnswer.setUpdateTime(now);

        boolean needJudge = false;
        List<ExamPaperQuestionCustomerAnswer> examPaperQuestionCustomerAnswers = examPaperQuestionCustomerAnswerService.selectListByPaperAnswerId(examPaperAnswer.getId());

        for (ExamPaperQuestionCustomerAnswer answer : examPaperQuestionCustomerAnswers) {
            QuestionTypeEnum questionType = QuestionTypeEnum.fromCode(answer.getQuestionType());
            if (questionType != null && QuestionTypeEnum.needSaveTextContent(questionType.getCode())) {
                needJudge = true;
                break;
            }
        }

        if (needJudge) {
            examPaperAnswer.setStatus(ExamPaperAnswerStatusEnum.WaitJudge.getCode());
        } else {
            examPaperAnswer.setStatus(ExamPaperAnswerStatusEnum.Complete.getCode());
        }

        examPaperAnswerMapper.updateByPrimaryKeySelective(examPaperAnswer);

        return ExamUtil.scoreToVM(examPaperAnswer.getUserScore());
    }

    /**
     * 脏数据自愈：早期版本保存答题记录时漏设了题型、序号、题目分值等字段，
     * 这里依据题目信息和前端提交的序号补齐，避免后续判分和更新出现 NPE。
     */
    private void repairQuestionAnswer(ExamPaperQuestionCustomerAnswer answer, Question question, Integer itemOrder) {
        if (question != null) {
            if (answer.getQuestionType() == null) {
                answer.setQuestionType(question.getQuestionType());
            }
            if (answer.getQuestionScore() == null) {
                answer.setQuestionScore(question.getScore());
            }
            if (answer.getQuestionTextContentId() == null) {
                answer.setQuestionTextContentId(question.getInfoTextContentId());
            }
            if (answer.getSubjectId() == null) {
                answer.setSubjectId(question.getSubjectId());
            }
        }
        if (answer.getItemOrder() == null && itemOrder != null) {
            answer.setItemOrder(itemOrder);
        }
    }

    /**
     * 客观题（单选/多选/判断）按最新答案重新判分；填空、简答题仅保存答案，等待人工批改。
     */
    private void judgeObjectiveAnswer(ExamPaperQuestionCustomerAnswer answer, ExamPaperSubmitItemVM itemVM, Question question) {
        QuestionTypeEnum questionType = QuestionTypeEnum.fromCode(answer.getQuestionType());
        if (questionType == null || question == null) {
            return;
        }
        switch (questionType) {
            case SingleChoice:
            case TrueFalse:
                answer.setAnswer(itemVM.getContent());
                answer.setDoRight(question.getCorrect() != null && question.getCorrect().equals(itemVM.getContent()));
                answer.setCustomerScore(Boolean.TRUE.equals(answer.getDoRight()) ? question.getScore() : 0);
                break;
            case MultipleChoice:
                if (itemVM.getContentArray() != null) {
                    String customerAnswer = ExamUtil.contentToString(itemVM.getContentArray());
                    answer.setAnswer(customerAnswer);
                    answer.setDoRight(customerAnswer.equals(question.getCorrect()));
                    answer.setCustomerScore(Boolean.TRUE.equals(answer.getDoRight()) ? question.getScore() : 0);
                }
                break;
            case GapFilling:
                if (itemVM.getContentArray() != null) {
                    answer.setAnswer(JsonUtil.toJsonStr(itemVM.getContentArray()));
                }
                answer.setCustomerScore(0);
                break;
            default:
                answer.setAnswer(itemVM.getContent());
                answer.setCustomerScore(0);
                break;
        }
    }

    private void updateQuestionAnswer(ExamPaperQuestionCustomerAnswer answer, ExamPaperSubmitItemVM itemVM) {
        QuestionTypeEnum questionType = QuestionTypeEnum.fromCode(answer.getQuestionType());
        if (questionType == null) {
            // 题型仍无法确定时仅兜底保存文本答案，不能再抛 NPE
            if (itemVM.getContent() != null) {
                answer.setAnswer(itemVM.getContent());
            }
            return;
        }
        switch (questionType) {
            case SingleChoice:
            case TrueFalse:
                answer.setAnswer(itemVM.getContent());
                break;
            case MultipleChoice:
                if (itemVM.getContentArray() != null) {
                    answer.setAnswer(ExamUtil.contentToString(itemVM.getContentArray()));
                }
                break;
            case GapFilling:
                if (itemVM.getContentArray() != null) {
                    answer.setAnswer(JsonUtil.toJsonStr(itemVM.getContentArray()));
                }
                break;
            default:
                answer.setAnswer(itemVM.getContent());
                break;
        }
    }

    private ExamPaperQuestionCustomerAnswer createQuestionAnswer(ExamPaperSubmitItemVM itemVM, Question question, ExamPaperAnswer examPaperAnswer, User user, Date now) {
        ExamPaperQuestionCustomerAnswer answer = new ExamPaperQuestionCustomerAnswer();
        answer.setQuestionId(itemVM.getQuestionId());
        answer.setExamPaperId(examPaperAnswer.getExamPaperId());
        answer.setExamPaperAnswerId(examPaperAnswer.getId());
        answer.setCreateTime(now);
        answer.setCreateUser(user.getId());
        answer.setItemOrder(itemVM.getItemOrder());
        if (question != null) {
            answer.setSubjectId(question.getSubjectId());
            answer.setQuestionType(question.getQuestionType());
            answer.setQuestionScore(question.getScore());
            answer.setQuestionTextContentId(question.getInfoTextContentId());
        } else {
            answer.setSubjectId(examPaperAnswer.getSubjectId());
        }
        judgeObjectiveAnswer(answer, itemVM, question);
        if (answer.getCustomerScore() == null) {
            answer.setCustomerScore(0);
        }
        return answer;
    }
}
