package com.lottery.interceptor;

import com.lottery.common.ResultCode;
import com.lottery.exception.BusinessException;
import com.lottery.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 */
@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!UserContext.isAdmin()) {
            log.warn("非管理员用户尝试访问管理接口: userId={}, username={}", 
                    UserContext.getUserId(), UserContext.getUsername());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return true;
    }
}
