package com.duoc.LearningPlatform.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLayerAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLayerAspect.class);

    @Pointcut("execution(* com.duoc.LearningPlatform.service..*(..))")
    public void serviceLayerMethods() {
    }

    @Around("serviceLayerMethods()")
    public Object logServiceCallDuration(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
            log.debug("service {} finished after {} ms", joinPoint.getSignature().toShortString(), elapsedMs);
        }
    }

    @AfterThrowing(pointcut = "serviceLayerMethods()", throwing = "exception")
    public void logServiceException(JoinPoint joinPoint, Throwable exception) {
        log.warn("service {} threw {}: {}",
                joinPoint.getSignature().toShortString(),
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }
}
