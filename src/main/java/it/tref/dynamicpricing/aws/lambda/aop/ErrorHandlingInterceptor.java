package it.tref.dynamicpricing.aws.lambda.aop;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.exception.JsonProcessingRuntimeException;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Interceptor that handles errors thrown by Lambda handler methods.
 * <p>
 * It intercepts method invocations and converts exceptions such as JSON processing errors,
 * validation errors, or other exceptions into an appropriate APIGatewayProxyResponseEvent.
 * Exceptions are also logged.
 * </p>
 */
@HandleErrors
@Interceptor
public class ErrorHandlingInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingInterceptor.class);

    @AroundInvoke
    public Object handleErrors(InvocationContext context) throws Exception {
        try {
            return context.proceed();
        } catch (JsonProcessingRuntimeException e) {
            logger.error("JSON processing error: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.BAD_REQUEST)
                    .withBody("Invalid request payload");
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.BAD_REQUEST)
                    .withBody(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal error: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .withBody("Error processing request");
        }
    }
}
