package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class AbstractHandler {
    /**
     * Processes the incoming API Gateway event and returns an HTTP response.
     */
    public abstract APIGatewayProxyResponseEvent handleEvent(APIGatewayProxyRequestEvent event);
}
