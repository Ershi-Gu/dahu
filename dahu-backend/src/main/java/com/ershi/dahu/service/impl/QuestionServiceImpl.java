package com.ershi.dahu.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ershi.dahu.common.BaseResponse;
import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.constant.CommonConstant;
import com.ershi.dahu.constant.AiGenerateQuestionConstant;
import com.ershi.dahu.exception.ThrowUtils;
import com.ershi.dahu.manager.AiManager;
import com.ershi.dahu.mapper.QuestionMapper;
import com.ershi.dahu.model.dto.question.AiGenerateQuestionRequest;
import com.ershi.dahu.model.dto.question.QuestionContentDTO;
import com.ershi.dahu.model.dto.question.QuestionQueryRequest;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.Question;
import com.ershi.dahu.model.entity.User;
import com.ershi.dahu.model.enums.AppTypeEnum;
import com.ershi.dahu.model.vo.QuestionVO;
import com.ershi.dahu.model.vo.UserVO;
import com.ershi.dahu.service.AppService;
import com.ershi.dahu.service.QuestionService;
import com.ershi.dahu.service.UserService;
import com.ershi.dahu.utils.SqlUtils;
import com.ershi.dahu.utils.UserHolder;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 题目表服务实现
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private AiManager aiManager;

    @Resource
    private Scheduler vipScheduler;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {

        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String questionContent = question.getQuestionContent();
        Long appId = question.getAppId();
        // 创建数据时，参数不能为空
        if (add) {
            // 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(questionContent), ErrorCode.PARAMS_ERROR, "题目内容不能为空");
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId 非法");
        }
        // 修改数据时，有参数则校验
        // 补充校验规则
        if (appId != null) {
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "绑定应用不存在");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        List<QuestionContentDTO> questionContentList = questionQueryRequest.getQuestionContentList();
        Long appId = questionQueryRequest.getAppId();
        String searchText = questionQueryRequest.getSearchText();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 模糊查询
        // JSON 数组查询
        if (CollUtil.isNotEmpty(questionContentList)) {
            for (QuestionContentDTO questionContentDTO : questionContentList) {
                queryWrapper.like("questionContent", questionContentDTO.getTitle());
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目表封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);
        questionVO.setQuestionContent(JSONUtil.toList(question.getQuestionContent(), QuestionContentDTO.class));

        // region 可选
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // endregion

        return questionVO;
    }

    /**
     * 分页获取题目表封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(QuestionVO::objToVo).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userService.getUserVO(user));
            questionList.forEach(question -> {
                String questionContent = question.getQuestionContent();
                List<QuestionContentDTO> questionContentDTOList = JSONUtil.toList(questionContent, QuestionContentDTO.class);
                questionVO.setQuestionContent(questionContentDTOList);
            });
        });
        // endregion

        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }


    /**
     * AI生成题目
     *
     * @param aiGenerateQuestionRequest
     * @return {@link BaseResponse}<{@link List}<{@link QuestionContentDTO}>>
     */
    @Override
    public List<QuestionContentDTO> aiGenerateQuestion(AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取请求信息
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();

        // 获取应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);

        // 封装prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);

        // 调用AI接口生成题目
        String resultJson = aiManager.doSyncAiTitleRequest(AiGenerateQuestionConstant.GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage);

        // 处理生成题目内容格式
        String result = resultJsonHandle(resultJson);
        return JSONUtil.toList(result, QuestionContentDTO.class);
    }

    /**
     * AI生成题目（流式）
     *
     * @param aiGenerateQuestionRequest
     * @return {@link SseEmitter}
     */
    @Override
    public SseEmitter aiGenerateQuestionBySSE(AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取请求信息
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();

        // 获取应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);

        // 封装prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);

        // 创建SSE连接对象
        SseEmitter sseEmitter = new SseEmitter(0L);
        // 调用AI接口生成题目（流式返回）
        Flowable<ModelData> modelDataFlowable
                = aiManager.doSSEAiTitleRequest(AiGenerateQuestionConstant.GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage);
        // 流式信息处理
        StringBuilder sb = new StringBuilder();
        AtomicInteger counter = new AtomicInteger();
        modelDataFlowable
                .observeOn(Schedulers.io())
                // 预处理
                // 1. 获取字符串
                .map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
                // 2. 去除特殊字符串，保留文本字符串
                .map(message -> message.replaceAll("\\s", ""))
                // 3. 过滤掉空字符
                .filter(StringUtils::isNotBlank)
                // 4. 将一个字符串转换为多个单独字符流，方便处理
                .flatMap(message -> {
                    List<Character> characterList = new ArrayList<>();
                    for (char c : message.toCharArray()) {
                        characterList.add(c);
                    }
                    return Flowable.fromIterable(characterList);
                })
                // 到这一步拿到的流就是每个字符单独的流，再做处理
                // 1. 截取一道完整的题目
                .doOnNext(c -> {
                    // 1.1 记录题目开头
                    if (c == '{') {
                        counter.addAndGet(1);
                    }
                    // 1.2 添加中间题目的内容
                    if (counter.get() > 0) {
                        sb.append(c);
                    }
                    // 1.3 通过}判断是否是题目结尾
                    if (c == '}') {
                        counter.addAndGet(-1);
                        // 一道完整的题目结尾
                        if (counter.get() == 0) {
                            // 通过SSE返回一道题目
                            sseEmitter.send(JSONUtil.toJsonStr(sb.toString()));
                            // 重置题目操作器，准备下一道题
                            sb.setLength(0);
                        }
                    }
                })
                .doOnError(e -> log.error("SSE Error ->", e))
                // 通知SSE完成
                .doOnComplete(sseEmitter::complete)
                .subscribe();

        return sseEmitter;
    }


    /**
     * VIP-AI生成题目（SSE实时推送）
     *
     * @param aiGenerateQuestionRequest
     * @return {@link SseEmitter}
     */
    @Override
    public SseEmitter aiGenerateQuestionBySSE_VIP(AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取请求信息
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();

        // 获取应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);

        // 封装prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);

        // 创建SSE连接对象
        SseEmitter sseEmitter = new SseEmitter(0L);
        // 调用AI接口生成题目（流式返回）
        Flowable<ModelData> modelDataFlowable
                = aiManager.doSSEAiTitleRequest(AiGenerateQuestionConstant.GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage);

        // 创建线程池
        Scheduler scheduler = Schedulers.io();
        // 获取用户信息，判断是否是VIP用户
        User loginUser = UserHolder.getUser();
        String userRole = loginUser.getUserRole();
        if ("VIP".equals(userRole)) {
            scheduler = vipScheduler;
        }

        // 流式信息处理
        StringBuilder sb = new StringBuilder();
        AtomicInteger counter = new AtomicInteger();
        modelDataFlowable
                .observeOn(scheduler)
                // 预处理
                // 1. 获取字符串
                .map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
                // 2. 去除特殊字符串，保留文本字符串
                .map(message -> message.replaceAll("\\s", ""))
                // 3. 过滤掉空字符
                .filter(StringUtils::isNotBlank)
                // 4. 将一个字符串转换为多个单独字符流，方便处理
                .flatMap(message -> {
                    List<Character> characterList = new ArrayList<>();
                    for (char c : message.toCharArray()) {
                        characterList.add(c);
                    }
                    return Flowable.fromIterable(characterList);
                })
                // 到这一步拿到的流就是每个字符单独的流，再做处理
                // 1. 截取一道完整的题目
                .doOnNext(c -> {
                    // 1.1 记录题目开头
                    if (c == '{') {
                        counter.addAndGet(1);
                    }
                    // 1.2 添加中间题目的内容
                    if (counter.get() > 0) {
                        sb.append(c);
                    }
                    // 1.3 通过}判断是否是题目结尾
                    if (c == '}') {
                        counter.addAndGet(-1);
                        // 一道完整的题目结尾
                        if (counter.get() == 0) {
                            // 通过SSE返回一道题目
                            sseEmitter.send(JSONUtil.toJsonStr(sb.toString()));
                            // 重置题目操作器，准备下一道题
                            sb.setLength(0);
                        }
                    }
                })
                .doOnError(e -> log.error("SSE Error ->", e))
                // 通知SSE完成
                .doOnComplete(sseEmitter::complete)
                .subscribe();

        return sseEmitter;
    }


    /**
     * 处理生成题目请求的AI输入的信息
     *
     * @param app            应用
     * @param questionNumber 题目数
     * @param optionNumber   选项数
     * @return {@link String} AI输入信息
     */
    @Override
    public String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("，").append("\n");
        userMessage.append(app.getAppDesc()).append("，").append("\n");
        userMessage.append(AppTypeEnum.getEnumByValue(app.getAppType()).getText()).append("，").append("\n");
        userMessage.append(questionNumber).append("，").append("\n");
        userMessage.append(optionNumber);
        return userMessage.toString();
    }


    /**
     * AI生成题目字符串处理
     *
     * @param resultJson
     * @return {@link String}
     */
    private String resultJsonHandle(String resultJson) {
        int start = resultJson.indexOf("[");
        int end = resultJson.lastIndexOf("]");
        return resultJson.substring(start, end + 1);
    }
}
