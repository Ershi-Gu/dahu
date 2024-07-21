package com.ershi.dahu.model.dto.question;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiGenerateQuestionRequest implements Serializable {

    /**
     *应用id
     */
    private Long appId;

    /**
     *题目数
     */
    private int questionNumber;

    /**
     *应用数
     */
    private int optionNumber;

    private static final long serialVersionUID = 5097148827102045361L;
}
