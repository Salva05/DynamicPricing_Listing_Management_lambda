package it.tref.dynamicpricing.aws.lambda.aop;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods or classes for automatic validation of incoming payloads.
 * <p>
 * When a class or method is annotated with {@code @ValidatePayload}, an interceptor will be invoked
 * to validate its parameters using the configured validation service. Any constraint violations found
 * will result in an exception being thrown.
 * </p>
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatePayload {
}