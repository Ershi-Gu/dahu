package com.ershi.dahu.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目答案封装类（用于AI评分）
 * @author Ershi
 * @date 2024/07/21
 */
@Data
public class QuestionAnswerDTO implements Serializable {

    /**
     *题目
     */
    private String title;

    /**
     *用户回答
     */
    private String userAnswer;

    private static final long serialVersionUID = -4433122453544342384L;
}
