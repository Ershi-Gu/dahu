package com.ershi.dahu.controller;

import com.ershi.dahu.common.BaseResponse;
import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.common.ResultUtils;
import com.ershi.dahu.exception.ThrowUtils;
import com.ershi.dahu.model.dto.statistic.AnswerResultCountDTO;
import com.ershi.dahu.model.dto.statistic.AppAnswerCountDTO;
import com.ershi.dahu.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.xml.transform.Result;
import java.util.List;

/**
 * App统计分析接口
 * @author Ershi
 * @date 2024/08/07
 */
@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class StatisticController {

    @Resource
    private StatisticService statisticService;



    @GetMapping("/app_answer_count")
    public BaseResponse<List<AppAnswerCountDTO>> getAppAnswerCount() {
        return ResultUtils.success(statisticService.getAppAnswerCount());

    }


    @GetMapping("/app_answer_result_count")
    public BaseResponse<List<AnswerResultCountDTO>> getAnswerResult(Long appId){
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(statisticService.getAnswerResultCount(appId));

    }
}
