package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.aop.HandleErrors;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import it.tref.dynamicpricing.aws.lambda.util.TokenUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

/**
 * AWS Lambda handler for deleting a listing.
 * <p>
 * This handler processes DELETE requests to remove a listing.
 * </p>
 */
@ApplicationScoped
public class DeleteListingHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeleteListingHandler.class);

    private final ListingService listingService;

    /**
     * Constructs a new DeleteListingHandler.
     *
     * @param listingService the service to handle business logic for listings.
     */
    public DeleteListingHandler(ListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * Handles DELETE requests for removing a listing.
     * Expects the 'listingId' to be present in the path parameters.
     *
     * @param event the API Gateway request event.
     * @return an APIGatewayProxyResponseEvent with status 204 No Content.
     */
    @Override
    @HandleErrors
    public APIGatewayProxyResponseEvent handleEvent(APIGatewayProxyRequestEvent event) {
        String listingId = event.getPathParameters().get("listingId");
        String userId = TokenUtil.extractUserIdFromEvent(event);
        logger.info("Deleting listing with ID {} for user {}", listingId, userId);

        listingService.deleteListing(listingId, userId);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.NO_CONTENT);
    }
}
