package com.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    @Around("execution(* com.ecommerce.service.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get("traceId");
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.debug("[{}] SERVICE_METHOD_START - {}.{} with args: {}", 
                traceId, className, methodName, Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.debug("[{}] SERVICE_METHOD_END - {}.{} completed in {}ms", 
                    traceId, className, methodName, duration);
            
            if (duration > 3000) {
                log.warn("[{}] SLOW_SERVICE_METHOD - {}.{} took {}ms", 
                        traceId, className, methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}] SERVICE_METHOD_ERROR - {}.{} failed after {}ms - Error: {}", 
                    traceId, className, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }

    @Around("execution(* com.ecommerce.repository.*.*(..))")
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get("traceId");
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.trace("[{}] REPOSITORY_METHOD_START - {}.{}", traceId, className, methodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.trace("[{}] REPOSITORY_METHOD_END - {}.{} completed in {}ms", 
                    traceId, className, methodName, duration);
            
            if (duration > 1000) {
                log.warn("[{}] SLOW_REPOSITORY_METHOD - {}.{} took {}ms", 
                        traceId, className, methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}] REPOSITORY_METHOD_ERROR - {}.{} failed after {}ms - Error: {}", 
                    traceId, className, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }
}