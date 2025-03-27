package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import it.tref.dynamicpricing.aws.lambda.dto.CreateListingRequest;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CreateListingHandlerTest {

    private MapperService mapperService;
    private ListingService listingService;
    private CreateListingHandler createListingHandler;

    @BeforeEach
    public void setUp() {
        mapperService = mock(MapperService.class);
        listingService = mock(ListingService.class);
        createListingHandler = new CreateListingHandler(mapperService, listingService);
    }

    @Test
    public void testHandleEventCreateSuccess() throws Exception {
        // Create a dummy JSON payload
        String jsonPayload = "{\"name\": \"Test Listing\", \"attributes\": {\"key\": \"value\"}}";

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody(jsonPayload);
        requestEvent.setHttpMethod("POST");

        // Set up a valid request context with authorizer claims
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext =
                new APIGatewayProxyRequestEvent.ProxyRequestContext();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com"); // Provide the expected email
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);
        proxyRequestContext.setAuthorizer(authorizer);
        requestEvent.setRequestContext(proxyRequestContext);

        // Prepare the expected CreateListingRequest DTO
        CreateListingRequest createListingRequest = new CreateListingRequest();
        createListingRequest.setName("Test Listing");

        // When mapperService.readValue is called, return our DTO
        when(mapperService.readValue(jsonPayload, CreateListingRequest.class)).thenReturn(createListingRequest);

        // Simulate the service returning a listing ID
        String dummyListingId = "dummy-id";
        when(listingService.createListing(eq(createListingRequest), anyString())).thenReturn(dummyListingId);

        // Call the handler
        APIGatewayProxyResponseEvent responseEvent = createListingHandler.handleEvent(requestEvent);

        // Verify that a 201 Created status is returned
        assertEquals(201, responseEvent.getStatusCode());
        // Verify that the Location header is set correctly
        assertNotNull(responseEvent.getHeaders());
        assertEquals("/listings/" + dummyListingId, responseEvent.getHeaders().get("Location"));

        // Verify that listingService.createListing() was called with the correct DTO
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(listingService, times(1)).createListing(eq(createListingRequest), userIdCaptor.capture());
        assertNotNull(userIdCaptor.getValue());
    }
}
