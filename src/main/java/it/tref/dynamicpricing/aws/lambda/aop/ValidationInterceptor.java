package it.tref.dynamicpricing.aws.lambda.aop;

import it.tref.dynamicpricing.aws.lambda.validation.ValidationService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interceptor that validates method parameters for classes or methods annotated with {@code @ValidatePayload}.
 * <p>
 * It uses the {@link ValidationService} to check all method parameters before proceeding with the invocation.
 * If any constraint violations are found, an {@link IllegalArgumentException} is thrown.
 * </p>
 */
@ValidatePayload
@Interceptor
public class ValidationInterceptor {

    @Inject
    private ValidationService validationService;

    @AroundInvoke
    public Object validateMethod(InvocationContext context) throws Exception {
        for (Object param : context.getParameters()) {
            if (param != null) {
                Set<ConstraintViolation<Object>> violations = validationService.validate(param);
                if (!violations.isEmpty()) {
                    String errorMessage = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(", "));
                    throw new IllegalArgumentException("Validation error: " + errorMessage);
                }
            }
        }
        return context.proceed();
    }
}