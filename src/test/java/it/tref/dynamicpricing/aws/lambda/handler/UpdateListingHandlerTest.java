package it.tref.dynamicpricing.aws.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import it.tref.dynamicpricing.aws.lambda.dto.UpdateListingRequest;
import it.tref.dynamicpricing.aws.lambda.exception.JsonProcessingRuntimeException;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import it.tref.dynamicpricing.aws.lambda.service.ListingService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class UpdateListingHandlerTest {

    @Inject
    private UpdateListingHandler updateListingHandler;
    private MapperService mapperService;
    private ListingService listingService;


    @BeforeEach
    public void setUp() {
        // Create mocks manually
        mapperService = mock(MapperService.class);
        listingService = mock(ListingService.class);

        // Install the mocks into the CDI container so that the interceptor is applied
        QuarkusMock.installMockForType(mapperService, MapperService.class);
        QuarkusMock.installMockForType(listingService, ListingService.class);
    }

    @Test
    public void testHandleEventUpdateSuccess() throws Exception {
        // Create a dummy JSON payload for updating a listing
        String jsonPayload = "{\"name\": \"Updated Listing\", \"attributes\": {\"color\": \"blue\"}}";

        // Create a fake request event
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody(jsonPayload);
        requestEvent.setHttpMethod("PUT");

        // Simulate path parameters containing the listingId
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("listingId", "test-id");
        requestEvent.setPathParameters(pathParams);

        // Set up a valid request context with authorizer claims
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext =
                new APIGatewayProxyRequestEvent.ProxyRequestContext();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "user@example.com"); // The expected user identifier
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);
        proxyRequestContext.setAuthorizer(authorizer);
        requestEvent.setRequestContext(proxyRequestContext);

        // Prepare the expected UpdateListingRequest DTO
        UpdateListingRequest updateRequest = new UpdateListingRequest();
        updateRequest.setName("Updated Listing");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("color", "blue");
        updateRequest.setAttributes(attributes);

        // When mapperService.readValue is called, return DTO
        when(mapperService.readValue(jsonPayload, UpdateListingRequest.class)).thenReturn(updateRequest);

        // Call the update handler
        APIGatewayProxyResponseEvent responseEvent = updateListingHandler.handleEvent(requestEvent);

        // Verify that a 204 No Content status is returned
        assertEquals(204, responseEvent.getStatusCode());
        // For a 204 response body is null
        assertNull(responseEvent.getBody());

        // Verify that listingService.updateListing() was called with the correct parameters
        ArgumentCaptor<UpdateListingRequest> dtoCaptor = ArgumentCaptor.forClass(UpdateListingRequest.class);
        ArgumentCaptor<String> listingIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(listingService, times(1)).updateListing(listingIdCaptor.capture(), dtoCaptor.capture(), userIdCaptor.capture());

        assertEquals("test-id", listingIdCaptor.getValue());
        assertEquals("Updated Listing", dtoCaptor.getValue().getName());
        assertEquals("user@example.com", userIdCaptor.getValue());
    }

    @Test
    public void testHandleEventUpdateInvalidPayload() throws Exception {
        String invalidJson = "invalid json";

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody(invalidJson);
        requestEvent.setHttpMethod("PUT");

        // Set up path parameters
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("listingId", "test-id");
        requestEvent.setPathParameters(pathParams);

        // Set up a valid request context with authorizer claims
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext =
                new APIGatewayProxyRequestEvent.ProxyRequestContext();
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "user@example.com");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);
        proxyRequestContext.setAuthorizer(authorizer);
        requestEvent.setRequestContext(proxyRequestContext);

        // When mapperService.readValue is called, simulate a JSON processing error by throwing a RuntimeException
        when(mapperService.readValue(invalidJson, UpdateListingRequest.class))
                .thenThrow(new JsonProcessingRuntimeException("Error deserializing JSON", new RuntimeException("Invalid request payload")));

        // Call the update handler
        APIGatewayProxyResponseEvent responseEvent = updateListingHandler.handleEvent(requestEvent);

        // The error-handling interceptor should catch this and return a 400 Bad Request
        assertEquals(400, responseEvent.getStatusCode());
        // Expect the error message in the body
        assertTrue(responseEvent.getBody().contains("Invalid request payload") ||
                responseEvent.getBody().contains("Error processing request"));
    }
}
