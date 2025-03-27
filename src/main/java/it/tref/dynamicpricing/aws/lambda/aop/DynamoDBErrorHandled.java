package it.tref.dynamicpricing.aws.lambda.aop;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods or classes for handling DynamoDB-related errors.
 * <p>
 * When a class or method is annotated with {@code @DynamoDBErrorHandled}, an interceptor
 * will be invoked to manage any errors that occur during DynamoDB operations.
 * </p>
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamoDBErrorHandled {
}
