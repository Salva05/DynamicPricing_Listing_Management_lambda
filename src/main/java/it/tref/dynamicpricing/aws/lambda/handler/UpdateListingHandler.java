package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.aop.HandleErrors;
import it.tref.dynamicpricing.aws.lambda.dto.UpdateListingRequest;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import it.tref.dynamicpricing.aws.lambda.util.TokenUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

/**
 * AWS Lambda handler for updating an existing Listing.
 * <p>
 * This handler deserializes the API Gateway event payload into an {@code UpdateListingRequest} DTO,
 * extracts the listingId from the URL path parameters and the user ID from token claims,
 * and delegates the update operation to the {@code ListingService}.
 * On successful update, it returns a 204 No Content response.
 * </p>
 */
@ApplicationScoped
public class UpdateListingHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateListingHandler.class);

    private final MapperService mapperService;
    private final ListingService listingService;

    /**
     * Constructs a new UpdateListingHandler.
     *
     * @param mapperService  the service to map JSON to/from objects.
     * @param listingService the service to handle business logic for listings.
     */
    public UpdateListingHandler(MapperService mapperService, ListingService listingService) {
        this.mapperService = mapperService;
        this.listingService = listingService;
    }

    /**
     * Processes the API Gateway request event for updating an existing listing and returns a response event.
     *
     * @param event the API Gateway request event.
     * @return the API Gateway response event with status 204 No Content on success.
     */
    @Override
    @HandleErrors
    public APIGatewayProxyResponseEvent handleEvent(APIGatewayProxyRequestEvent event) {
        String body = event.getBody();
        logger.info("Update request body: {}", body);

        UpdateListingRequest updateListingRequest = mapperService.readValue(body, UpdateListingRequest.class);

        // Extract listingId from URL path parameters
        String listingId = event.getPathParameters().get("listingId");

        String userId = TokenUtil.extractUserIdFromEvent(event);

        listingService.updateListing(listingId, updateListingRequest, userId);

        // 204 - NO CONTENT
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.NO_CONTENT);
    }
}
