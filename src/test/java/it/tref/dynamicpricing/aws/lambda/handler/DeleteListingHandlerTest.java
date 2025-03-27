package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DeleteListingHandlerTest {
    private ListingService listingService;
    private DeleteListingHandler deleteListingHandler;

    @BeforeEach
    public void setUp() {
        listingService = mock(ListingService.class);
        deleteListingHandler = new DeleteListingHandler(listingService);
    }

    @Test
    public void testDeleteListingHandlerSuccess() {
        // Set up request event with a "listingId" path parameter and authorizer claims.
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> pathParams = new HashMap<>();
        String listingId = "testId";
        pathParams.put("listingId", listingId);
        requestEvent.setPathParameters(pathParams);

        APIGatewayProxyRequestEvent.ProxyRequestContext proxyContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        Map<String, Object> authorizer = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "user@example.com");
        authorizer.put("claims", claims);
        proxyContext.setAuthorizer(authorizer);
        requestEvent.setRequestContext(proxyContext);

        // Call the handler.
        APIGatewayProxyResponseEvent responseEvent = deleteListingHandler.handleEvent(requestEvent);

        // Assert that the response status is 204 No Content.
        assertEquals(204, responseEvent.getStatusCode());

        // Verify that listingService.deleteListing() was called with the correct parameters.
        verify(listingService, times(1)).deleteListing(eq(listingId), eq("user@example.com"));
    }
}
