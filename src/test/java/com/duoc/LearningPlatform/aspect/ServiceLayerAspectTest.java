package com.duoc.LearningPlatform.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceLayerAspectTest {

    private final ServiceLayerAspect aspect = new ServiceLayerAspect();
    private ListAppender<ILoggingEvent> logAppender;
    private Logger aspectLogger;

    @BeforeEach
    void attachLogCapture() {
        aspectLogger = (Logger) LoggerFactory.getLogger(ServiceLayerAspect.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        aspectLogger.addAppender(logAppender);
        aspectLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void detachLogCapture() {
        aspectLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void aroundReturnsProceedResultAndLogsDebugCompletion() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("PaymentService.create()");
        when(joinPoint.proceed()).thenReturn(42);

        Object result = aspect.logServiceCallDuration(joinPoint);

        assertEquals(42, result);
        verify(joinPoint).proceed();
        assertTrue(logAppender.list.stream().anyMatch(e ->
                e.getLevel() == Level.DEBUG && e.getFormattedMessage().contains("PaymentService.create()")));
    }

    @Test
    void aroundPropagatesExceptionFromProceed() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("UserService.find()");
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("rule broken"));

        assertThrows(IllegalStateException.class, () -> aspect.logServiceCallDuration(joinPoint));
    }

    @Test
    void afterThrowingLogsWarningWithExceptionDetails() {
        JoinPoint joinPoint = mock(JoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("CourseService.get()");

        aspect.logServiceException(joinPoint, new IllegalArgumentException("invalid id"));

        assertTrue(logAppender.list.stream().anyMatch(e ->
                e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("CourseService.get()")
                        && e.getFormattedMessage().contains("IllegalArgumentException")
                        && e.getFormattedMessage().contains("invalid id")));
    }
}
