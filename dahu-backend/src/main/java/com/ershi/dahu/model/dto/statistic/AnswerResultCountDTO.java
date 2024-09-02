package com.ershi.dahu.model.dto.statistic;

import lombok.Data;

import java.io.Serializable;

/**
 * 答题结果统计
 *
 *
 */
@Data
public class AnswerResultCountDTO implements Serializable {

    /**
     * 结果名称
     */
    private String resultName;

    /**
     * 结果数
     */
    private Long resultCount;

    private static final long serialVersionUID = 1010860539698779754L;
}