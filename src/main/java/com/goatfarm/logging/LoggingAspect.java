package com.goatfarm.logging;

import com.goatfarm.exception.BusinessRuleViolationException;
import com.goatfarm.exception.InvalidCredentialException;
import com.goatfarm.exception.ResourceNotFoundException;
import com.goatfarm.model.AuthUser;
import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.StringJoiner;

/**
 * Cross-cutting call logging for controllers and services: entry/exit at DEBUG (enable with the "dev" profile),
 * slow calls at WARN and unexpected failures at ERROR so they are visible in production. Business events
 * and business-rule rejections (a goat was added, a login failed ...) are logged by the services themselves.
 *
 * <p>Sensitive data never reaches the log: arguments named like a password/token/secret are masked, and
 * the credential-carrying DTOs mask themselves in {@code toString()}. Failures are logged here as a single line
 * (type + message); full stack traces of unexpected errors are logged once by the exception handler.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final int MAX_ARG_LENGTH = 150;
    /** Calls slower than this are reported at WARN so slow endpoints show up in monitoring. */
    private static final long SLOW_CALL_MS = 1000;

    @Pointcut("within(com.goatfarm.controller..*)")
    public void controllerLayer() {
    }

    @Pointcut("within(com.goatfarm.service..*)")
    public void serviceLayer() {
    }

    @Around("controllerLayer()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAround(joinPoint, "CONTROLLER");
    }

    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAround(joinPoint, "SERVICE");
    }

    private Object logAround(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String userInfo = describeCurrentUser();

        if (log.isDebugEnabled()) {
            log.debug("[->] {} {}.{}({}), userInfo={}", layer, className, methodName, describeArguments(joinPoint), userInfo);
        }
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed >= SLOW_CALL_MS) {
                log.warn("[<-] {} {}.{}() [SLOW] {} ms, userInfo={}", layer, className, methodName, elapsed, userInfo);
            } else {
                log.debug("[<-] {} {}.{}() [OK] {} ms", layer, className, methodName, elapsed);
            }
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            if (isExpected(ex)) {
                log.debug("[<-] {} {}.{}() [REJECTED] {} ms, {}: {}", layer, className, methodName, elapsed,
                        ex.getClass().getSimpleName(), ex.getMessage());
            } else {
                log.error("[<-] {} {}.{}() [ERROR] {} ms, userInfo={}", layer, className, methodName, elapsed, userInfo);
            }
            throw ex; // behaviour must not change: always rethrow
        }
    }

    /** Business outcomes (404, 400, 401): not bugs, so no ERROR level and no stack trace (services log the notable ones). */
    private static boolean isExpected(Throwable ex) {
        return ex instanceof ResourceNotFoundException
                || ex instanceof BusinessRuleViolationException
                || ex instanceof InvalidCredentialException
                || ex instanceof EntityNotFoundException
                || ex instanceof IllegalArgumentException;
    }

    private String describeArguments(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (names == null || names.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < names.length && i < args.length; i++) {
            joiner.add(names[i] + "=" + safeValue(names[i], args[i]));
        }
        return joiner.toString();
    }

    private static String safeValue(String name, Object value) {
        if (value == null) {
            return "null";
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("password") || lower.contains("token") || lower.contains("secret")) {
            return "***";
        }
        String text = String.valueOf(value);
        return text.length() > MAX_ARG_LENGTH ? text.substring(0, MAX_ARG_LENGTH) + "..." : text;
    }

    private static String describeCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthUser user) {
            return "{ userId=" + user.userId() + ", farmId=" + user.farmId() + " }";
        }
        return "";
    }
}
