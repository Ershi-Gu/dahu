package com.ershi.dahu.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.ershi.dahu.constant.CaptchaConstant;
import com.ershi.dahu.model.vo.CaptchaVO;
import com.ershi.dahu.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 生成验证码并存储到 Redis
    public CaptchaVO createCaptcha() {

        // 生成图片验证码
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(
                CaptchaConstant.CAPTCHA_IMAGE_LENGTH,
                CaptchaConstant.CAPTCHA_IMAGE_WEIGHT,
                CaptchaConstant.CAPTCHA_IMAGE_CONTENT_LENGTH,
                CaptchaConstant.CAPTCHA_IMAGE_INTERFERE_WEIGHT);
        // 获取验证码字符串
        String captchaCode = captcha.getCode();
        log.info("验证码 = {}", captchaCode);

        // 生成 token
        String token = IdUtil.simpleUUID();

        // 将验证码图片转为 Base64 编码
        String captchaBase64 = Base64.encode(captcha.getImageBytes());

        // 将验证码和 token 存储到 Redis
        stringRedisTemplate.opsForValue().set(CaptchaConstant.CAPTCHA_KEY_PREFIX + token, captchaCode,
                CaptchaConstant.CAPTCHA_TIMEOUT, TimeUnit.MINUTES); // 设置 5 分钟过期时间

        // 创建 CaptchaVO 对象并设置属性
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaBase64(captchaBase64);
        captchaVO.setToken(token);

        return captchaVO;
    }

    /**
     * 校验验证码
     *
     * @param captcha
     * @param token
     * @return boolean
     */
    public boolean checkCaptcha(String captcha, String token) {
        if (StringUtils.isAnyBlank(captcha, token)) {
            log.info("请求参数错误");
            return false;
        }

        // 根据 token 从 Redis 取出验证码
        String checkCaptcha = stringRedisTemplate.opsForValue().get(CaptchaConstant.CAPTCHA_KEY_PREFIX + token);
        if (StringUtils.isBlank(checkCaptcha)) {
            log.info("验证码不存在");
            return false;
        }

        // 验证码校验匹配
        if (!captcha.equals(checkCaptcha)) {
            log.info("验证码不匹配");
            return false;
        }

        return true;
    }
}
