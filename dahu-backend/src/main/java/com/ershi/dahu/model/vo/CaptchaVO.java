package com.ershi.dahu.model.vo;

/**
 * 返回验证码
 * @author Ershi
 * @date 2024/06/05
 */
public class CaptchaVO {

    private String captchaBase64;

    private String token;

    public String getCaptchaBase64() {
        return captchaBase64;
    }

    public void setCaptchaBase64(String captchaBase64) {
        this.captchaBase64 = captchaBase64;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}