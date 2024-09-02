package com.ershi.dahu.service.impl;

import com.ershi.dahu.mapper.UserAnswerMapper;
import com.ershi.dahu.model.dto.statistic.AnswerResultCountDTO;
import com.ershi.dahu.model.dto.statistic.AppAnswerCountDTO;
import com.ershi.dahu.service.StatisticService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Resource
    private UserAnswerMapper userAnswerMapper;


    @Override
    public List<AppAnswerCountDTO> getAppAnswerCount() {
        return userAnswerMapper.getAppAnswerCount();
    }

    @Override
    public List<AnswerResultCountDTO> getAnswerResultCount(Long appId) {
        return userAnswerMapper.getAnswerResultCount(appId);
    }
}
