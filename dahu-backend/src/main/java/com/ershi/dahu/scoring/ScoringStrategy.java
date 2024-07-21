package com.ershi.dahu.scoring;

import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.UserAnswer;
import io.swagger.models.auth.In;

import java.util.List;

/**
 * 执行评分策略接口
 * @author Ershi
 * @date 2024/07/11
 */
public interface ScoringStrategy {

    /**
     * 执行评分
     *
     * @param app 请求执行评分的应用
     * @param choices 用户回答
     * @return {@link UserAnswer}
     */
    UserAnswer doScoring(App app, List<String> choices);
}
