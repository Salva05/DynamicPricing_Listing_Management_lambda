package it.tref.dynamicpricing.aws.lambda.aop;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark Lambda handler methods or classes for automatic error handling.
 * <p>
 * When applied, an interceptor will catch exceptions thrown during the method execution
 * and convert them into a proper {@code APIGatewayProxyResponseEvent}.
 * </p>
 */
@InterceptorBinding
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface HandleErrors {
}
