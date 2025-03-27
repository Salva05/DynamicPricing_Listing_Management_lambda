package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.dto.GetListingResponse;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RetrieveListingHandlerTest {

    private MapperService mapperService;
    private ListingService listingService;
    private RetrieveListingHandler retrieveListingHandler;

    @BeforeEach
    public void setUp() {
        mapperService = mock(MapperService.class);
        listingService = mock(ListingService.class);
        retrieveListingHandler = new RetrieveListingHandler(mapperService, listingService);
    }

    @Test
    public void testHandleEventRetrieveSuccess() throws Exception {
        // Set up request event with path parameter and simulated authorizer claims.
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> pathParams = new HashMap<>();
        String listingId = "listing-123";
        pathParams.put("listingId", listingId);
        requestEvent.setPathParameters(pathParams);

        APIGatewayProxyRequestEvent.ProxyRequestContext context = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        Map<String, Object> authorizer = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "user@example.com");
        authorizer.put("claims", claims);
        context.setAuthorizer(authorizer);
        requestEvent.setRequestContext(context);

        Listing listing = new Listing();
        listing.setListingId(listingId);
        listing.setUserId("user@example.com");
        listing.setName("Test Listing");
        listing.setCreatedAt(Instant.now());

        when(listingService.getListing(eq(listingId), anyString())).thenReturn(listing);

        GetListingResponse responseDto = new GetListingResponse();
        responseDto.setListing(listing);
        String responseBody = "{\"listingId\":\"" + listingId + "\",\"name\":\"Test Listing\"}";
        when(mapperService.writeValueAsString(any(GetListingResponse.class))).thenReturn(responseBody);

        APIGatewayProxyResponseEvent responseEvent = retrieveListingHandler.handleEvent(requestEvent);

        assertEquals(200, responseEvent.getStatusCode());
        assertEquals(responseBody, responseEvent.getBody());

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(listingService, times(1)).getListing(eq(listingId), userIdCaptor.capture());
        assertEquals("user@example.com", userIdCaptor.getValue());
    }
}
