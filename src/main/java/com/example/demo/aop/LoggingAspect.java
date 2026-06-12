package com.example.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.example.demo.service.*.*(..))")
    public Object LogExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();
        String username = "Guest";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            username = authentication.getName();
        }

        String methodName = joinPoint.getSignature().toShortString();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.error("[AOP LOG] User '{}' đã thực hiện thất bại (bị lỗi) method {} sau {} ms", username, methodName, timeTaken);
            throw throwable;
        }

        long timeTaken = System.currentTimeMillis() - startTime;
        log.info("[AOP LOG] User '{}' đã thực hiện thành công method {} trong {} ms", username, methodName, timeTaken);
        return result;
    }

}
