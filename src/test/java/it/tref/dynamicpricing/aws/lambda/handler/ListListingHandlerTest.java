package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.dto.ListListingsResponse;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ListListingHandlerTest {

    private MapperService mapperService;
    private ListingService listingService;
    private ListListingHandler listListingHandler;

    @BeforeEach
    public void setUp() {
        mapperService = mock(MapperService.class);
        listingService = mock(ListingService.class);
        listListingHandler = new ListListingHandler(mapperService, listingService);
    }

    @Test
    public void testHandleEventListSuccess() throws Exception {
        // Set up request event with simulated authorizer claims.
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        APIGatewayProxyRequestEvent.ProxyRequestContext context = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        Map<String, Object> authorizer = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "user@example.com");
        authorizer.put("claims", claims);
        context.setAuthorizer(authorizer);
        requestEvent.setRequestContext(context);

        // Prepare two Listing objects.
        Listing listing1 = new Listing();
        listing1.setListingId("listing-1");
        listing1.setUserId("user@example.com");
        listing1.setName("Listing One");
        listing1.setCreatedAt(Instant.now());

        Listing listing2 = new Listing();
        listing2.setListingId("listing-2");
        listing2.setUserId("user@example.com");
        listing2.setName("Listing Two");
        listing2.setCreatedAt(Instant.now());

        when(listingService.listListings(anyString())).thenReturn(Arrays.asList(listing1, listing2));

        String jsonResponse = "{\"listings\":[{\"listingId\":\"listing-1\",\"name\":\"Listing One\"}," +
                "{\"listingId\":\"listing-2\",\"name\":\"Listing Two\"}]}";
        when(mapperService.writeValueAsString(any(ListListingsResponse.class))).thenReturn(jsonResponse);

        APIGatewayProxyResponseEvent responseEvent = listListingHandler.handleEvent(requestEvent);

        assertEquals(200, responseEvent.getStatusCode());
        assertEquals(jsonResponse, responseEvent.getBody());
    }
}
