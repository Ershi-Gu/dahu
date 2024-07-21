package com.ershi.dahu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ershi.dahu.common.BaseResponse;
import com.ershi.dahu.model.dto.question.AiGenerateQuestionRequest;
import com.ershi.dahu.model.dto.question.QuestionContentDTO;
import com.ershi.dahu.model.dto.question.QuestionQueryRequest;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.Question;
import com.ershi.dahu.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目表服务
 *
 *
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    
    /**
     * 获取题目表封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目表封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);


    /**
     * AI生成题目
     * @param aiGenerateQuestionRequest
     * @return {@link BaseResponse}<{@link List}<{@link QuestionContentDTO}>>
     */
    List<QuestionContentDTO> aiGenerateQuestion(AiGenerateQuestionRequest aiGenerateQuestionRequest );


    /**
     * 处理生成题目请求的AI输入的信息
     * @param app 应用
     * @param questionNumber 题目数
     * @param optionNumber 选项数
     * @return {@link String} AI输入信息
     */
    String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber);
}
