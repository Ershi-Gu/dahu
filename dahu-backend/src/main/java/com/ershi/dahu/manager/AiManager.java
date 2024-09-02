package com.ershi.dahu.manager;

import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AiManager {

    @Resource
    private ClientV4 clientV4;

    // 稳定的温度随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    // 不稳定的温度随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;

    // 稳定的top随机数
    private static final float STABLE_TOP = 0.05f;

    // 不稳定的top随机数
    private static final float UNSTABLE_TOP = 0.95f;

    // AI答题应用平台生成题目用温度随机数
    private static final float AI_ANSWER_TEMPERATURE = 0.80f;

    // AI答题应用平台生成题目用top随机数
    private static final float AI_ANSWER_TOP_P = 0.70f;

    // AI答题应用平台评测用温度随机数
    private static final float AI_TEST_TEMPERATURE = 0.80f;

    // AI答题应用平台评测用top随机数
    private static final float AI_TEST_TOP_P = 0.70f;


    /**
     * AI评测请求（同步）
     *
     * @param systemMessage 系统预设prompt
     * @param userMessage   用户输入
     * @return {@link String}
     */
    public String doSyncAiTestRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, AI_TEST_TEMPERATURE, AI_TEST_TOP_P);
    }


    /**
     * AI答题应用平台生成题目请求（同步）
     *
     * @param systemMessage 系统预设prompt
     * @param userMessage   用户输入
     * @return {@link String}
     */
    public String doSyncAiTitleRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, AI_ANSWER_TEMPERATURE, AI_ANSWER_TOP_P);
    }


    /**
     * AI答题应用平台生成题目请求（流式）
     * @param systemMessage
     * @param userMessage
     * @return {@link Flowable}<{@link ModelData}>
     */
    public Flowable<ModelData> doSSEAiTitleRequest(String systemMessage, String userMessage){
        return doStreamRequest(systemMessage, userMessage, AI_ANSWER_TEMPERATURE, AI_ANSWER_TOP_P);
    }


    /**
     * 同步请求（答案不稳定）
     *
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncUnstableRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, UNSTABLE_TEMPERATURE, UNSTABLE_TOP);
    }

    /**
     * 同步请求（答案较稳定）
     *
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncStableRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, STABLE_TEMPERATURE, STABLE_TOP);
    }


    /**
     * 通用同步请求（简化消息传递）
     *
     * @param systemMessage 系统预设prompt
     * @param userMessage   用户输入内容
     * @param temperature
     * @return
     */
    public String doSyncRequest(String systemMessage, String userMessage, Float temperature, Float topP) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        chatMessageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        chatMessageList.add(userChatMessage);
        return doRequest(chatMessageList, temperature, topP);
    }

    /**
     * 通用同步请求
     *
     * @param messages    消息列表
     * @param stream      是否流式传输，否-同步，是-异步
     * @param temperature 温度随机值
     * @return AI生成的内容字符串
     */
    public String doRequest(List<ChatMessage> messages, Float temperature, Float topP) {
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(false)
                .temperature(temperature)
                .topP(topP)
                .invokeMethod(Constants.invokeMethod)
                .tools(buildWebSearchTools())
                .maxTokens(4096)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }


    /**
     * 通用流式请求（简化消息传递）
     *
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @param topP
     * @return {@link Flowable}<{@link ModelData}>
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature, Float topP) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        chatMessageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        chatMessageList.add(userChatMessage);
        return doStreamRequest(chatMessageList, temperature, topP);
    }


    /**
     * 通用流式请求
     *
     * @param messages
     * @param temperature
     * @param topP
     * @return {@link Flowable}<{@link ModelData}>
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature, Float topP) {
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(true)
                .temperature(temperature)
                .topP(topP)
                .invokeMethod(Constants.invokeMethod)
                .tools(buildWebSearchTools())
                .maxTokens(4096)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getFlowable();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }


    /**
     * 构建网络搜索工具
     *
     * @return {@link List}<{@link ChatTool}>
     */
    private List<ChatTool> buildWebSearchTools() {
        ChatTool chatTool = new ChatTool();
        chatTool.setType("web_search");
        WebSearch webSearch = new WebSearch();
        webSearch.setEnable(true);
        chatTool.setWeb_search(webSearch);
        ArrayList<ChatTool> tools = new ArrayList<>();
        tools.add(chatTool);
        return tools;
    }
}
