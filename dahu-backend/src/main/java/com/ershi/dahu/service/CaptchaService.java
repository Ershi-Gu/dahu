package com.ershi.dahu.service;

import com.ershi.dahu.model.vo.CaptchaVO;

public interface CaptchaService {

    /**
     * 生成验证码
     * @return {@link CaptchaVO}
     */
    CaptchaVO createCaptcha();

    /**
     * 校验验证码
     *
     * @param captcha
     * @param token
     * @return boolean
     */
    boolean checkCaptcha(String captcha, String token);
}
