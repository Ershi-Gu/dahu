package com.ershi.dahu.controller;


import com.ershi.dahu.common.BaseResponse;
import com.ershi.dahu.common.ResultUtils;
import com.ershi.dahu.model.vo.CaptchaVO;
import com.ershi.dahu.service.impl.CaptchaServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 验证码接口
 * @author Ershi
 * @date 2024/06/05
 */
@RestController
@Slf4j
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private CaptchaServiceImpl captchaService;


    /**
     * 获取验证码
     */
    @GetMapping("/get")
    public BaseResponse<CaptchaVO> getCaptcha() {
        CaptchaVO captchaVO = captchaService.createCaptcha();
        return ResultUtils.success(captchaVO);
    }
}
