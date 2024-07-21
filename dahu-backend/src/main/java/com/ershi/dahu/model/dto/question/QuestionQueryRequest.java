package com.ershi.dahu.model.dto.question;

import com.ershi.dahu.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询题目请求
 *
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 除外id
     */
    private Long notId;

    /**
     * 题目内容（json格式）
     */
    private List<QuestionContentDTO> questionContentList;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}