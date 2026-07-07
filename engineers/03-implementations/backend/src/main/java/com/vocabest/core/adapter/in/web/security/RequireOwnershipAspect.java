package com.vocabest.core.adapter.in.web.security;

import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
public class RequireOwnershipAspect {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(requireOwnership)")
    public Object checkOwnershipMethod(ProceedingJoinPoint pjp, RequireOwnership requireOwnership) {
        return checkOwnership(pjp, requireOwnership);
    }
    
    @Around("@within(requireOwnership) && !@annotation(com.vocabest.core.adapter.in.web.security.RequireOwnership)")
    public Object checkOwnershipClass(ProceedingJoinPoint pjp, RequireOwnership requireOwnership) {
        return checkOwnership(pjp, requireOwnership);
    }

    private Object checkOwnership(ProceedingJoinPoint pjp, RequireOwnership requireOwnership) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = discoverer.getParameterNames(method);
        Object[] args = pjp.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        UUID targetUserId = null;
        try {
            targetUserId = parser.parseExpression(requireOwnership.value()).getValue(context, UUID.class);
        } catch (Exception e) {
            // Expression evaluation failed or returned null.
            // targetUserId remains null.
        }
        
        final UUID finalTargetUserId = targetUserId;
        Class<?> returnType = signature.getReturnType();

        if (Mono.class.isAssignableFrom(returnType)) {
            return Mono.deferContextual(ctx -> {
                User user = ctx.getOrDefault("CURRENT_USER", null);
                if (user == null) {
                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated"));
                }
                if (user.role() != Role.ADMIN) {
                    if (finalTargetUserId == null || !user.id().equals(finalTargetUserId)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Target user ID is missing or does not match current user."));
                    }
                }
                try {
                    return (Mono<?>) pjp.proceed();
                } catch (Throwable t) {
                    return Mono.error(t);
                }
            });
        } else if (Flux.class.isAssignableFrom(returnType)) {
            return Flux.deferContextual(ctx -> {
                User user = ctx.getOrDefault("CURRENT_USER", null);
                if (user == null) {
                    return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated"));
                }
                if (user.role() != Role.ADMIN) {
                    if (finalTargetUserId == null || !user.id().equals(finalTargetUserId)) {
                        return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Target user ID is missing or does not match current user."));
                    }
                }
                try {
                    return (Flux<?>) pjp.proceed();
                } catch (Throwable t) {
                    return Flux.error(t);
                }
            });
        }
        
        // Fallback for non-reactive return types (should not happen in this application)
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
