package it.tref.dynamicpricing.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.handler.CreateListingHandler;
// import it.tref.dynamicpricing.aws.lambda.handler.DeleteListingHandler;
// import it.tref.dynamicpricing.aws.lambda.handler.GetListingHandler;
// import it.tref.dynamicpricing.aws.lambda.handler.UpdateListingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

/**
 * Entry point for the Listing Management Lambda.
 * <p>
 * This class implements the AWS Lambda RequestHandler interface and routes incoming
 * API Gateway events to the appropriate handler (create, update, delete, get listing).
 * The API Gateway is configured with a Cognito authorizer so that only authenticated
 * requests reach this entrypoint.
 * </p>
 */
public class ListingManagementLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // W.I.P.

    private static final Logger logger = LoggerFactory.getLogger(ListingManagementLambda.class);

    private final CreateListingHandler createListingHandler;
    // private final UpdateListingHandler updateListingHandler;
    // private final DeleteListingHandler deleteListingHandler;
    // private final GetListingHandler getListingHandler;

    /**
     * Constructs a new ListingManagementLambda with the given handlers.
     *
     * @param createListingHandler the handler for creating listings.
     * // @param updateListingHandler the handler for updating listings.
     * // @param deleteListingHandler the handler for deleting listings.
     * // @param getListingHandler    the handler for retrieving listings.
     */
    public ListingManagementLambda(CreateListingHandler createListingHandler
                                   // UpdateListingHandler updateListingHandler,
                                   // DeleteListingHandler deleteListingHandler,
                                   // GetListingHandler getListingHandler
    ) {
        this.createListingHandler = createListingHandler;
        // this.updateListingHandler = updateListingHandler;
        // this.deleteListingHandler = deleteListingHandler;
        // this.getListingHandler = getListingHandler;
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

        // Route based on the HTTP method (you might also use path parameters or resource URIs)
        switch (method) {
            case "POST":
                return createListingHandler.handleEvent(input);
            // case "PUT":
                // return updateListingHandler.handleEvent(input);
            // case "GET":
                // return getListingHandler.handleEvent(input);
            // case "DELETE":
                // return deleteListingHandler.handleEvent(input);
            default:
                logger.warn("Unsupported HTTP method: {}", method);
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatusCode.BAD_REQUEST)
                        .withBody("Unsupported HTTP method");
        }
    }
}
