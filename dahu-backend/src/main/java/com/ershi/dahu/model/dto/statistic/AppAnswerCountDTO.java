package com.ershi.dahu.model.dto.statistic;

import lombok.Data;

import java.io.Serializable;

/**
 * App答题情况统计dto
 *
 *
 */
@Data
public class AppAnswerCountDTO implements Serializable {

    /**
     * appId
     */
    private Long appId;

    /**
     *应用名
     */
    private String appName;

    /**
     * 总答题数
     */
    private Long userAnswerCount;

    private static final long serialVersionUID = -9205463449731380208L;
}