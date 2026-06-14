package com.goatfarm.logging;

import com.goatfarm.model.AuthUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Controller methods (adjust package if yours is com.goatfarm.controller)
    @Pointcut("within(com.goatfarm.controller..*)")
    public void controllerLayer() {}

    // Service methods
    @Pointcut("within(com.goatfarm.service..*)")
    public void serviceLayer() {}

    // Around advice for Controllers
    @Around("controllerLayer()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAround(joinPoint, "CONTROLLER");
    }

    // Around advice for Services
    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAround(joinPoint, "SERVICE");
    }

    private Object logAround(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String argString = buildArgsString(joinPoint);

        AuthUser authUser = getAuthUser();
        String userInfo = authUser != null
                ? ("{ userId=" + authUser.getUserId() + ", farmId=" + authUser.getFarmId() + " }")
                : "";

        log.info("[->] {} {}.{}({}), userInfo={}", layer, className, methodName, argString, userInfo);

        try {
            Object result = joinPoint.proceed();
            long time = System.currentTimeMillis() - start;
            log.info("[<-] {} {}.{}() [OK] {} ms", layer, className, methodName, time);
            return result;
        } catch (Throwable ex) {
            long time = System.currentTimeMillis() - start;
            log.error("[<-] {} {}.{}() [ERROR] {} ms",
                    layer, className, methodName, time,
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex,
                    userInfo);
            throw ex; // IMPORTANT: rethrow so behavior doesn't change
        }
    }

    /**
     * Builds parameter string safely
     * - avoids printing huge objects
     * - masks password fields
     */
    private String buildArgsString(ProceedingJoinPoint joinPoint) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = sig.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames == null || paramNames.length == 0) {
            return "";
        }

        return Arrays.stream(paramNames)
                .map(name -> name + "=" + safeArgValue(name, args[indexOf(paramNames, name)]))
                .collect(Collectors.joining(", "));
    }

    private int indexOf(String[] arr, String key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(key)) return i;
        }
        return 0;
    }

    private String safeArgValue(String name, Object val) {
        if (val == null) return "null";

        // Do not log passwords/tokens
        String lower = name.toLowerCase();
        if (lower.contains("password") || lower.contains("token")) {
            return "***";
        }

        // Keep log short (avoid huge JSON logs)
        String s = String.valueOf(val);
        if (s.length() > 150) {
            return s.substring(0, 150) + "...";
        }

        return s;
    }

    private AuthUser getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof AuthUser au) {
            return au;
        }
        return null;
    }
}