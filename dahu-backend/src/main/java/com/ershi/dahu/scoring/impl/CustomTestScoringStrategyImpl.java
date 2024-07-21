package com.ershi.dahu.scoring.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ershi.dahu.model.dto.question.QuestionContentDTO;
import com.ershi.dahu.model.dto.question.QuestionOption;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.Question;
import com.ershi.dahu.model.entity.ScoringResult;
import com.ershi.dahu.model.entity.UserAnswer;
import com.ershi.dahu.model.enums.AppTypeEnum;
import com.ershi.dahu.model.enums.ScoringStrategyEnum;
import com.ershi.dahu.model.vo.QuestionVO;
import com.ershi.dahu.scoring.ScoringStrategy;
import com.ershi.dahu.scoring.ScoringStrategyConfig;
import com.ershi.dahu.service.QuestionService;
import com.ershi.dahu.service.ScoringResultService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 评测类应用执行策略实现类
 *
 * @author Ershi
 * @date 2024/07/11
 */
@ScoringStrategyConfig(appType = AppTypeEnum.TEST, scoringStrategy = ScoringStrategyEnum.CUSTOM)
public class CustomTestScoringStrategyImpl implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScoring(App app, List<String> choices) {
        // 1. 获取应用对应的题目列表，以及应用对应的所有结果集合
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
        );

        // 2. 统计用户答案对应与题目每个结果属性的匹配次数，以MBTI为例，比如"I":9,"N":12
        HashMap<String, Integer> optionCount = new HashMap<>();

        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 遍历题目列表
        for (QuestionContentDTO questionContentDTO : questionContent) {
            // 遍历答案列表
            for (String answer : choices) {
                // 遍历题目中的选项
                for (QuestionOption option : questionContentDTO.getOptions()) {
                    // 如果答案和选项的key匹配
                    if (option.getKey().equals(answer)) {
                        // 获取选项的result属性
                        String result = option.getResult();
                        // 如果result属性不在optionCount中，初始化为0
                        if (!optionCount.containsKey(result)) {
                            optionCount.put(result, 0);
                        }
                        // 在optionCount中增加计数
                        optionCount.put(result, optionCount.get(result) + 1);
                    }
                }
            }
        }

        // 3. 遍历每种评分结果，计算哪个结果的得分更高，返回最高得分的结果作为评分结果
        // 初始化评分结果
        int maxScore = 0;
        ScoringResult maxScoringResult = scoringResultList.get(0);

        // 遍历每个结果集，根据用户答案计算当前结果集的得分
        for (ScoringResult scoringResult : scoringResultList) {
            List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
            int score = resultProp.stream().mapToInt(porp -> optionCount.getOrDefault(porp, 0))
                    .sum();

            // 如果当前结果集的分大于最高的分的结果集，替换
            if (score > maxScore) {
                maxScoringResult = scoringResult;
            }
        }

        // 4. 返回用户回答结果记录表
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());

        return userAnswer;
    }
}
