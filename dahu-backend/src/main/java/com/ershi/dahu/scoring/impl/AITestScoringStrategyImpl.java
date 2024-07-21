package com.ershi.dahu.scoring.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ershi.dahu.constant.AiGenerateQuestionConstant;
import com.ershi.dahu.manager.AiManager;
import com.ershi.dahu.model.dto.question.QuestionAnswerDTO;
import com.ershi.dahu.model.dto.question.QuestionContentDTO;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.Question;
import com.ershi.dahu.model.entity.UserAnswer;
import com.ershi.dahu.model.enums.AppTypeEnum;
import com.ershi.dahu.model.enums.ScoringStrategyEnum;
import com.ershi.dahu.model.vo.QuestionVO;
import com.ershi.dahu.scoring.ScoringStrategy;
import com.ershi.dahu.scoring.ScoringStrategyConfig;
import com.ershi.dahu.service.QuestionService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 评测类应用执行策略实现类
 *
 * @author Ershi
 * @date 2024/07/11
 */
@ScoringStrategyConfig(appType = AppTypeEnum.TEST, scoringStrategy = ScoringStrategyEnum.AI)
public class AITestScoringStrategyImpl implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    @Override
    public UserAnswer doScoring(App app, List<String> choices) {
        // 1. 获取应用对应的题目列表
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 2. 封装prompt
        String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
        String systemMessage = AiGenerateQuestionConstant.GENERATE_RESULT_SYSTEM_MESSAGE;

        // 3. 调用AI接口
        String resultJson = aiManager.doSyncAiTestRequest(systemMessage, userMessage);
        String result = resultJsonHandle(resultJson);


        // 4. 返回用户回答结果记录表
        UserAnswer userAnswer = JSONUtil.toBean(result, UserAnswer.class);
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));

        return userAnswer;
    }


    /**
     * 封装用于AI测评的用户输入答案
     *
     * @param app
     * @param questionContentDTOList
     * @param choices
     * @return {@link String}
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("，").append("\n");
        userMessage.append(app.getAppDesc()).append("，").append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }

    /**
     * AI生成题目字符串处理
     *
     * @param resultJson
     * @return {@link String}
     */
    private String resultJsonHandle(String resultJson) {
        int start = resultJson.indexOf("{");
        int end = resultJson.lastIndexOf("}");
        return resultJson.substring(start, end + 1);
    }
}
