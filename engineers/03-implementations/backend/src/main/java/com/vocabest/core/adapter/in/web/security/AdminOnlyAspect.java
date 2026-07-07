package com.vocabest.core.adapter.in.web.security;

import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class AdminOnlyAspect {

    @Around("@annotation(com.vocabest.core.adapter.in.web.security.AdminOnly) || @within(com.vocabest.core.adapter.in.web.security.AdminOnly)")
    public Object checkAdminAccess(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (Mono.class.isAssignableFrom(returnType)) {
            return Mono.deferContextual(ctx -> {
                User user = ctx.getOrDefault("CURRENT_USER", null);
                if (user == null || user.role() != Role.ADMIN) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required"));
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
                if (user == null || user.role() != Role.ADMIN) {
                    return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required"));
                }
                try {
                    return (Flux<?>) pjp.proceed();
                } catch (Throwable t) {
                    return Flux.error(t);
                }
            });
        }
        
        throw new IllegalStateException("@AdminOnly can only be applied to methods returning Mono or Flux");
    }
}
