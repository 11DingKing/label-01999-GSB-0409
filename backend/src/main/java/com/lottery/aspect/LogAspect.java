package com.lottery.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("execution(* com.lottery.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Long userId = UserContext.getUserId();

        // 记录请求日志
        try {
            String args = objectMapper.writeValueAsString(joinPoint.getArgs());
            log.info("[请求] {}.{} | userId={} | args={}", className, methodName, userId, args);
        } catch (Exception e) {
            log.info("[请求] {}.{} | userId={} | args=序列化失败", className, methodName, userId);
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[异常] {}.{} | userId={} | cost={}ms | error={}", 
                    className, methodName, userId, costTime, e.getMessage());
            throw e;
        }

        // 记录响应日志
        long costTime = System.currentTimeMillis() - startTime;
        log.info("[响应] {}.{} | userId={} | cost={}ms", className, methodName, userId, costTime);

        return result;
    }
}
