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

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 评分类应用执行策略实现类
 *
 * @author Ershi
 * @date 2024/07/11
 */
@ScoringStrategyConfig(appType = AppTypeEnum.SCORE, scoringStrategy = ScoringStrategyEnum.CUSTOM)
public class CustomScoreScoringStrategyImpl implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScoring(App app, List<String> choices) {
        Long appId = app.getId();
        // 1. 根据 id 查询到题目和题目结果信息（按分数降序排序）
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );

        // 2. 统计用户的总得分
        int totalScore = 0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 将答案列表转换为集合，以提高查找效率
        Set<String> answerSet = new HashSet<>(choices);

        // 遍历题目列表
        for (QuestionContentDTO questionContentDTO : questionContent) {
            // 遍历题目中的选项
            for (QuestionOption option : questionContentDTO.getOptions()) {
                // 如果选项的key在答案集合中
                if (answerSet.contains(option.getKey())) {
                    totalScore += option.getScore();
                }
            }
        }


        // 3. 遍历得分结果，找到第一个用户分数大于得分范围的结果，作为最终结果
        ScoringResult maxScoringResult = scoringResultList.get(0);
        for (ScoringResult scoringResult : scoringResultList) {
            if (totalScore >= scoringResult.getResultScoreRange()) {
                maxScoringResult = scoringResult;
                break;
            }
        }

        // 4. 构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}
