package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.aop.HandleErrors;
import it.tref.dynamicpricing.aws.lambda.dto.ListListingsResponse;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import it.tref.dynamicpricing.aws.lambda.util.TokenUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;

/**
 * AWS Lambda handler for listing all listings associated with the authenticated user.
 * <p>
 * This handler processes GET requests to retrieve all listings for a user.
 * The response payload contains a list of Listing domain objects.
 * </p>
 */
@ApplicationScoped
public class ListListingHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListListingHandler.class);

    private final MapperService mapperService;
    private final ListingService listingService;

    /**
     * Constructs a new ListListingHandler.
     *
     * @param mapperService  the service to map JSON to/from objects.
     * @param listingService the service to handle business logic for listings.
     */
    public ListListingHandler(MapperService mapperService, ListingService listingService) {
        this.mapperService = mapperService;
        this.listingService = listingService;
    }

    /**
     * Handles GET requests for listing all listings associated with the user.
     *
     * @param event the API Gateway request event.
     * @return an APIGatewayProxyResponseEvent containing the list of listings.
     */
    @Override
    @HandleErrors
    public APIGatewayProxyResponseEvent handleEvent(APIGatewayProxyRequestEvent event) {
        String userId = TokenUtil.extractUserIdFromEvent(event);
        logger.info("Listing all listings for user {}", userId);

        List<Listing> listings = listingService.listListings(userId);

        ListListingsResponse responseDto = new ListListingsResponse();
        responseDto.setListings(listings);

        String responseBody = mapperService.writeValueAsString(responseDto);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.OK)
                .withBody(responseBody);
    }
}
