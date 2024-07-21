package com.ershi.dahu.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionContentDTO {

    /**
     *选项列表
     */
    private List<QuestionOption> options;

    /**
     *题目标题
     */
    private String title;
}
