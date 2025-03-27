package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.aop.HandleErrors;
import it.tref.dynamicpricing.aws.lambda.dto.GetListingResponse;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import it.tref.dynamicpricing.aws.lambda.util.TokenUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

/**
 * AWS Lambda handler for retrieving a single listing.
 * <p>
 * This handler processes GET requests to fetch a single listing using the listingId from path parameters.
 * The userId is extracted from the token claims.
 * </p>
 */
@ApplicationScoped
public class RetrieveListingHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateListingHandler.class);

    private final MapperService mapperService;
    private final ListingService listingService;

    /**
     * Constructs a new RetrieveListingHandler.
     *
     * @param mapperService  the service to map JSON to/from objects.
     * @param listingService the service to handle business logic for listings.
     */
    public RetrieveListingHandler(MapperService mapperService, ListingService listingService) {
        this.mapperService = mapperService;
        this.listingService = listingService;
    }

    /**
     * Handles GET requests for retrieving a single listing.
     * Expects the 'listingId' to be present in the path parameters.
     *
     * @param event the API Gateway request event.
     * @return an APIGatewayProxyResponseEvent with the listing details.
     */

    @Override
    @HandleErrors
    public APIGatewayProxyResponseEvent handleEvent(APIGatewayProxyRequestEvent event) {
        String listingId = event.getPathParameters().get("listingId");
        String userId = TokenUtil.extractUserIdFromEvent(event);
        logger.info("Fetching listing with ID {} for user {}", listingId, userId);

        Listing listing = listingService.getListing(listingId, userId);

        GetListingResponse responseDto = new GetListingResponse();
        responseDto.setListing(listing);

        String responseBody = mapperService.writeValueAsString(responseDto);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.OK)
                .withBody(responseBody);
    }
}
