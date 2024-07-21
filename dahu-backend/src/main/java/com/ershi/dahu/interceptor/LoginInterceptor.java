package com.ershi.dahu.interceptor;

import com.ershi.dahu.common.BaseResponse;
import com.ershi.dahu.common.ErrorCode;
import com.ershi.dahu.exception.BusinessException;
import com.ershi.dahu.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 1.判断是否需要拦截（ThreadLocal中是否有用户）
        if (UserHolder.getUser() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 有用户，则放行
        return true;
    }
}
