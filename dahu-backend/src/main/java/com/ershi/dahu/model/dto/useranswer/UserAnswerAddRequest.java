package com.ershi.dahu.model.dto.useranswer;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建用户答题记录表请求
 *
 *
 */
@Data
public class UserAnswerAddRequest implements Serializable {

    /**
     * 本次答题的Id(用于保证幂等性)
     */
    private Long id;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;

    private static final long serialVersionUID = 1L;
}