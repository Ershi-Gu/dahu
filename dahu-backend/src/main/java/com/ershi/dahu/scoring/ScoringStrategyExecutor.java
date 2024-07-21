package com.ershi.dahu.scoring;

import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.exception.BusinessException;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.UserAnswer;
import com.ershi.dahu.model.enums.AppTypeEnum;
import com.ershi.dahu.model.enums.ScoringStrategyEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 *
 * @author Ershi
 * @date 2024/07/11
 */
@Component
public class ScoringStrategyExecutor {

    @Resource
    private List<ScoringStrategy> scoringStrategyList;

    /**
     * 执行评分策略
     *
     * @param app
     * @param choices
     * @return {@link UserAnswer}
     */
    public UserAnswer doScoring(App app, List<String> choices) {
        // 1. 参数校验
        AppTypeEnum appType = AppTypeEnum.getEnumByValue(app.getAppType());
        ScoringStrategyEnum appScoringStrategy = ScoringStrategyEnum.getEnumByValue(app.getScoringStrategy());
        if (appType == null || appScoringStrategy == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
        }

        // 2. 根据请求评分的应用执行不同策略
        // 2.1 通过注解匹配策略
        for (ScoringStrategy scoringStrategy : scoringStrategyList) {
            if (scoringStrategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                ScoringStrategyConfig scoringStrategyConfig =
                        scoringStrategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (scoringStrategyConfig.appType()
                        == appType && scoringStrategyConfig.scoringStrategy()
                        == appScoringStrategy) {
                    return scoringStrategy.doScoring(app, choices);
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
    }

}
