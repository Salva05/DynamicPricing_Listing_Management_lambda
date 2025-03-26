package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import it.tref.dynamicpricing.aws.lambda.dto.CreateListingRequest;
import it.tref.dynamicpricing.aws.lambda.dto.CreateListingResponse;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import it.tref.dynamicpricing.aws.lambda.util.TokenUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;

/**
 * AWS Lambda handler for creating a new Listing.
 * <p>
 * This handler deserializes the API Gateway event payload into a CreateListingRequest DTO,
 * extracts the user ID from token claims, delegates the business logic to the ListingService,
 * and serializes the response.
 * </p>
 */
@ApplicationScoped
public class CreateListingHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateListingHandler.class);

    private final MapperService mapperService;
    private final ListingService listingService;

    /**
     * Constructs a new CreateListingHandler.
     *
     * @param mapperService  the service to map JSON to/from objects.
     * @param listingService the service to handle business logic for listings.
     */
    public CreateListingHandler(MapperService mapperService, ListingService listingService) {
        this.mapperService = mapperService;
        this.listingService = listingService;
    }

    /**
     * Processes the API Gateway request event and returns a response event.
     *
     * @param event the API Gateway request event.
     * @return the API Gateway response event.
     */
    @Override
    public APIGatewayProxyResponseEvent handleEvent(APIGatewayProxyRequestEvent event) {
        try {
            String body = event.getBody();
            logger.info("Request body is {}", body);

            // Deserialize the request payload into the CreateListingRequest DTO
            CreateListingRequest createListingRequest =
                    mapperService.readValue(body, CreateListingRequest.class);

            // Extract useId
            String userId = TokenUtil.extractUserIdFromEvent(event);

            // Delegate creation to the ListingService
            CreateListingResponse response = listingService.createListing(createListingRequest, userId);
            String responseBody = mapperService.writeValueAsString(response);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.CREATED)
                    .withBody(responseBody);
        } catch (JsonProcessingException e) {
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
            logger.error("Error creating listing: {}", e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                    .withBody("Error creating listing");
        }
    }
}
