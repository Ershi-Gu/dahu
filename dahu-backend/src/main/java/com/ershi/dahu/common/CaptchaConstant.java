package com.ershi.dahu.common;

public interface CaptchaConstant {

    /**
     * 图形验证码长、宽、内容长度、扰流线宽度
     */
    int CAPTCHA_IMAGE_LENGTH = 200;
    int CAPTCHA_IMAGE_WEIGHT = 100;
    int CAPTCHA_IMAGE_CONTENT_LENGTH = 4;
    int CAPTCHA_IMAGE_INTERFERE_WEIGHT = 200;

    Long CAPTCHA_TIMEOUT = 5L;

    String CAPTCHA_KEY_PREFIX = "captcha_";

}
