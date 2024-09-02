package com.ershi.dahu.scoring.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.constant.AiGenerateQuestionConstant;
import com.ershi.dahu.exception.BusinessException;
import com.ershi.dahu.manager.AiManager;
import com.ershi.dahu.model.dto.question.QuestionAnswerDTO;
import com.ershi.dahu.model.dto.question.QuestionContentDTO;
import com.ershi.dahu.model.entity.App;
import com.ershi.dahu.model.entity.Question;
import com.ershi.dahu.model.entity.RedisData;
import com.ershi.dahu.model.entity.UserAnswer;
import com.ershi.dahu.model.enums.AppTypeEnum;
import com.ershi.dahu.model.enums.ScoringStrategyEnum;
import com.ershi.dahu.model.vo.QuestionVO;
import com.ershi.dahu.scoring.ScoringStrategy;
import com.ershi.dahu.scoring.ScoringStrategyConfig;
import com.ershi.dahu.service.QuestionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ershi.dahu.constant.RedisCacheConstant.*;

/**
 * 评测类应用执行策略实现类
 *
 * @author Ershi
 * @date 2024/07/11
 */
@ScoringStrategyConfig(appType = AppTypeEnum.TEST, scoringStrategy = ScoringStrategyEnum.AI)
public class AITestScoringStrategyImpl implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public UserAnswer doScoring(App app, List<String> choices) {
        // 1. 获取应用对应的题目列表
        Long appId = app.getId();
        Question question = questionService.getOne(Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId));
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 2. 去缓存中查询评价
        String cacheKey = AI_TEST_SCORING_RESULT + buildAiScoringCacheKey(appId, choices);
        String resultByCache = stringRedisTemplate.opsForValue().get(cacheKey);

        // 3. 缓存命中
        if (StringUtils.isNotBlank(resultByCache)) {
            // 3.1 构建UserAnswer返回
            return buildUserAnswer(resultByCache, app, choices);
        }

        // 4. 缓存未命中，重建缓存
        // 4.1 定义锁
        String lockKey = LOCK_AI_TEST + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 4.2 获取互斥锁
            boolean isLock = lock.tryLock(5, 15, TimeUnit.SECONDS);

            // 4.3 判断是否获取成功
            if (!isLock) {
                // 4.4 失败，强行返回
                return null;
            }
            // 4.4 成功
            // 二次验证缓存
            String doubleCheckCache = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.isNotBlank(doubleCheckCache)) {
                return buildUserAnswer(doubleCheckCache, app, choices);
            }

            // 4.4.1 封装prompt
            String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
            String systemMessage = AiGenerateQuestionConstant.GENERATE_RESULT_SYSTEM_MESSAGE;

            // 4.4.2 调用AI接口
            String resultJson = aiManager.doSyncAiTestRequest(systemMessage, userMessage);
            String result = resultJsonHandle(resultJson);

            // 4.4.3 重建缓存
            stringRedisTemplate.opsForValue().set(cacheKey, result, AI_TEST_SCORING_RESULT_TTL, TimeUnit.DAYS);

            // 5. 构建答案表返回
            return buildUserAnswer(result, app, choices);

        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取锁失败");

        } finally {
            if (lock != null && lock.isLocked()) {
                // 只有当前获取锁的线程能释放当前锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 封装用于AI测评的用户输入答案
     *
     * @param app
     * @param questionContentDTOList
     * @param choices
     * @return {@link String}
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("，").append("\n");
        userMessage.append(app.getAppDesc()).append("，").append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }

    /**
     * AI生成题目字符串处理
     *
     * @param resultJson
     * @return {@link String}
     */
    private String resultJsonHandle(String resultJson) {
        int start = resultJson.indexOf("{");
        int end = resultJson.lastIndexOf("}");
        return resultJson.substring(start, end + 1);
    }

    /**
     * 构建AI评价缓存Key
     *
     * @param appId   应用id
     * @param choices 用户输入答案数组
     * @return {@link String}
     */
    private String buildAiScoringCacheKey(Long appId, List<String> choices) {
        String jsonChoices = JSONUtil.toJsonStr(choices);
        return DigestUtil.md5Hex(appId + ":" + jsonChoices);
    }


    /**
     * 构建用户回答表
     *
     * @param result
     * @param app
     * @param choices
     * @return {@link UserAnswer}
     */
    private UserAnswer buildUserAnswer(String result, App app, List<String> choices) {
        UserAnswer userAnswer = JSONUtil.toBean(result, UserAnswer.class);
        userAnswer.setAppId(app.getId());
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        return userAnswer;
    }
}
