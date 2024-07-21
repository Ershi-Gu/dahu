package com.ershi.dahu.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {

    /**
     *如果是测评类，则用 result 来保存答案属性
     */
    private String result;

    /**
     *如果是得分类，则用 score 来设置本题分数
     */
    private Integer score;

    /**
     *选项内容
     */
    private String value;

    /**
     *选项 key
     */
    private String key;
}
