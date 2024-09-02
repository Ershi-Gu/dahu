package com.ershi.dahu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ershi.dahu.model.dto.statistic.AnswerResultCountDTO;
import com.ershi.dahu.model.dto.statistic.AppAnswerCountDTO;
import com.ershi.dahu.model.entity.UserAnswer;

import java.util.List;

/**
 * App答题统计分析表
 * @author Ershi
 * @date 2024/08/07
 */
public interface StatisticService {

    /**
     * 统计查询每个应用用户答题情况（top10）
     * @return {@link List}<{@link AppAnswerCountDTO}>
     */
    List<AppAnswerCountDTO> getAppAnswerCount();


    /**
     * 统计指定应用中结果情况
     * @param appId
     * @return {@link List}<{@link AnswerResultCountDTO}>
     */
    List<AnswerResultCountDTO> getAnswerResultCount(Long appId);
}
