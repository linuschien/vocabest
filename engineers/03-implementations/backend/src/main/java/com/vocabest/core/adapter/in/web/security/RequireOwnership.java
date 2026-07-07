package com.vocabest.core.adapter.in.web.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method or controller requires the CURRENT_USER
 * to either be an ADMIN or own the resource being requested.
 * 
 * The {@code value} should be a SpEL expression that resolves to the UUID 
 * of the target user. If the expression resolves to null, a LEARNER will be 
 * denied access (403), while an ADMIN will be allowed.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOwnership {
    
    /**
     * SpEL expression to evaluate the target userId.
     * Examples: 
     * - "#userId" for a path variable
     * - "#filter?.userId()" for a GraphQL FilterInput object
     * @return SpEL expression string
     */
    String value();
}
