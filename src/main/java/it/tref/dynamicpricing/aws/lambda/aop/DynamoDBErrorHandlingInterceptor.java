package it.tref.dynamicpricing.aws.lambda.aop;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputExceededException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

/**
 * Interceptor that handles errors arising from DynamoDB operations.
 * <p>
 * This interceptor is bound to methods or classes annotated with
 * {@code @DynamoDBErrorHandled}. It intercepts method invocations and logs
 * DynamoDB-related exceptions.
 * </p>
 */
@Interceptor
@DynamoDBErrorHandled
public class DynamoDBErrorHandlingInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBErrorHandlingInterceptor.class);

    @AroundInvoke
    public Object handleDynamoDBErrors(InvocationContext ctx) throws Exception {
        try {
            return ctx.proceed();
        } catch (ResourceNotFoundException rnfe) {
            logger.error("Resource not found in method {}. Error: {}. Verify resource exists.",
                    ctx.getMethod().getName(), rnfe.getMessage(), rnfe);
            throw rnfe;
        } catch (ConditionalCheckFailedException ccfe) {
            logger.error("Conditional check failed in method {}. Error: {}. Validate preconditions.",
                    ctx.getMethod().getName(), ccfe.getMessage(), ccfe);
            throw ccfe;
        } catch (ProvisionedThroughputExceededException ptee) {
            logger.error("Throughput exceeded in method {}. Error: {}. Review provisioned limits.",
                    ctx.getMethod().getName(), ptee.getMessage(), ptee);
            throw ptee;
        } catch (DynamoDbException dde) {
            logger.error("DynamoDB exception in method {}. Error: {}.",
                    ctx.getMethod().getName(), dde.getMessage(), dde);
            throw dde;
        }
    }
}
