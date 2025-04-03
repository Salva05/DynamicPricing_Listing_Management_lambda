package it.tref.dynamicpricing.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.config.ConfigService;
import it.tref.dynamicpricing.aws.lambda.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point for the Listing Management Lambda.
 * <p>
 * This class implements the AWS Lambda RequestHandler interface and routes incoming
 * API Gateway events to the appropriate handler (create, update, delete, retrieve and list listing).
 * The API Gateway is configured with a Cognito authorizer so that only authenticated
 * requests reach this entrypoint.
 * </p>
 */
public class ListingManagementLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ListingManagementLambda.class);

    private final CreateListingHandler createListingHandler;
    private final UpdateListingHandler updateListingHandler;
    private final RetrieveListingHandler retrieveListingHandler;
    private final ListListingHandler listListingHandler;
    private final DeleteListingHandler deleteListingHandler;
    private final ConfigService configService;

    /**
     * Constructs a new ListingManagementLambda with the given handlers.
     *
     * @param createListingHandler  the handler for creating listings.
     * @param updateListingHandler  the handler for updating listings.
     * @param retrieveListingHandler the handler for retrieving listings.
     * @param deleteListingHandler  the handler for deleting listings.
     * @param listListingHandler    the handler for listing all listings.
     */
    public ListingManagementLambda(CreateListingHandler createListingHandler,
                                   UpdateListingHandler updateListingHandler,
                                   RetrieveListingHandler retrieveListingHandler,
                                   DeleteListingHandler deleteListingHandler,
                                   ListListingHandler listListingHandler,
                                   ConfigService configService) {
        this.createListingHandler = createListingHandler;
        this.updateListingHandler = updateListingHandler;
        this.retrieveListingHandler = retrieveListingHandler;
        this.deleteListingHandler = deleteListingHandler;
        this.listListingHandler = listListingHandler;
        this.configService = configService;
    }

    /**
     * Handles the incoming API Gateway request and routes it based on the HTTP method.
     *
     * @param input   the API Gateway request event.
     * @param context the Lambda execution context.
     * @return the API Gateway response event.
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        logger.info("Received request: {}", input);
        String method = input.getHttpMethod();
        logger.info("HTTP Method: {}", method);

        APIGatewayProxyResponseEvent response;
        switch (method) {
            case "POST":
                response = createListingHandler.handleEvent(input);
                break;
            case "PUT":
                response = updateListingHandler.handleEvent(input);
                break;
            case "DELETE":
                response = deleteListingHandler.handleEvent(input);
                break;
            case "GET":
                Map<String, String> pathParams = input.getPathParameters();
                if (pathParams != null && pathParams.containsKey("listingId")) {
                    response = retrieveListingHandler.handleEvent(input);
                } else {
                    response = listListingHandler.handleEvent(input);
                }
                break;
            default:
                logger.warn("Unsupported HTTP method: {}", method);
                response = new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatusCode.BAD_REQUEST)
                        .withBody("Unsupported HTTP method");
                break;
        }

        // Ensure headers are initialized and add CORS headers
        if (response.getHeaders() == null) {
            response.setHeaders(new HashMap<>());
        }
        response.getHeaders().put("Access-Control-Allow-Origin", configService.getDomainUrl());
        response.getHeaders().put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().put("Access-Control-Allow-Headers", "Content-Type, Authorization");

        return response;
    }
}
