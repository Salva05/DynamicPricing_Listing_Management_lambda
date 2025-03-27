package it.tref.dynamicpricing.aws.lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(DynamoDBTestResource.class)
public class ListingManagementLambdaTest {

    @Inject
    ListingManagementLambda lambda;

    @Test
    public void testFullCRUDFlow() {
        Context context = new DummyContext();

        // Prepare common request context with Cognito claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "integration@test.com");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        proxyContext.setAuthorizer(authorizer);

        // --- 1. Create Listing (POST) ---
        APIGatewayProxyRequestEvent createEvent = new APIGatewayProxyRequestEvent();
        createEvent.setHttpMethod("POST");
        createEvent.setBody("{\"name\":\"Integration Test Listing\", \"attributes\":{\"color\":\"blue\"}}");
        createEvent.setRequestContext(proxyContext);

        APIGatewayProxyResponseEvent createResponse = lambda.handleRequest(createEvent, context);
        assertEquals(201, createResponse.getStatusCode(), "Expected HTTP 201 for creation");
        String locationHeader = createResponse.getHeaders().get("Location");
        assertNotNull(locationHeader, "Location header must be set");
        // Extract listingId from location; format is "/listings/{listingId}"
        String listingId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
        assertFalse(listingId.isEmpty(), "Listing ID should be extracted");

        // --- 2. Retrieve Listing (GET with listingId) ---
        APIGatewayProxyRequestEvent retrieveEvent = new APIGatewayProxyRequestEvent();
        retrieveEvent.setHttpMethod("GET");
        Map<String, String> retrievePathParams = new HashMap<>();
        retrievePathParams.put("listingId", listingId);
        retrieveEvent.setPathParameters(retrievePathParams);
        retrieveEvent.setRequestContext(proxyContext);

        APIGatewayProxyResponseEvent retrieveResponse = lambda.handleRequest(retrieveEvent, context);
        assertEquals(200, retrieveResponse.getStatusCode(), "Expected HTTP 200 on retrieval");
        String retrieveBody = retrieveResponse.getBody();
        assertNotNull(retrieveBody);
        assertTrue(retrieveBody.contains("Integration Test Listing"));

        // --- 3. Update Listing (PUT) ---
        APIGatewayProxyRequestEvent updateEvent = new APIGatewayProxyRequestEvent();
        updateEvent.setHttpMethod("PUT");
        Map<String, String> updatePathParams = new HashMap<>();
        updatePathParams.put("listingId", listingId);
        updateEvent.setPathParameters(updatePathParams);
        updateEvent.setBody("{\"name\":\"Updated Test Listing\"}");
        updateEvent.setRequestContext(proxyContext);

        APIGatewayProxyResponseEvent updateResponse = lambda.handleRequest(updateEvent, context);
        // Update returns HTTP 204 No Content
        assertEquals(204, updateResponse.getStatusCode(), "Expected HTTP 204 on update");

        // --- 4. Retrieve Listing Again to Confirm Update ---
        APIGatewayProxyResponseEvent retrieveAfterUpdate = lambda.handleRequest(retrieveEvent, context);
        assertEquals(200, retrieveAfterUpdate.getStatusCode());
        assertTrue(retrieveAfterUpdate.getBody().contains("Updated Test Listing"),
                "Listing name should be updated");

        // --- 5. List All Listings (GET without listingId) ---
        APIGatewayProxyRequestEvent listEvent = new APIGatewayProxyRequestEvent();
        listEvent.setHttpMethod("GET");
        // Setting path parameters to null to trigger the list branch.
        listEvent.setPathParameters(null);
        listEvent.setRequestContext(proxyContext);

        // Delay for the DB' GSI to update.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        APIGatewayProxyResponseEvent listResponse = lambda.handleRequest(listEvent, context);
        assertEquals(200, listResponse.getStatusCode(), "Expected HTTP 200 for list operation");
        assertTrue(listResponse.getBody().contains(listingId), "List should include the created listing");

        // --- 6. Delete Listing (DELETE) ---
        APIGatewayProxyRequestEvent deleteEvent = new APIGatewayProxyRequestEvent();
        deleteEvent.setHttpMethod("DELETE");
        Map<String, String> deletePathParams = new HashMap<>();
        deletePathParams.put("listingId", listingId);
        deleteEvent.setPathParameters(deletePathParams);
        deleteEvent.setRequestContext(proxyContext);

        APIGatewayProxyResponseEvent deleteResponse = lambda.handleRequest(deleteEvent, context);
        assertEquals(204, deleteResponse.getStatusCode(), "Expected HTTP 204 for deletion");

        // --- 7. Attempt to Retrieve Deleted Listing ---
        APIGatewayProxyResponseEvent retrieveAfterDelete = lambda.handleRequest(retrieveEvent, context);
        // Error handling interceptor converts not found errors to HTTP 400.
        assertEquals(400, retrieveAfterDelete.getStatusCode(), "Expected HTTP 400 for missing listing");
        assertTrue(retrieveAfterDelete.getBody().contains("Listing not found"), "Error message should indicate missing listing");
    }

    // Dummy AWS Lambda context
    private static class DummyContext implements Context {
        @Override public String getAwsRequestId() { return "dummy-request-id"; }
        @Override public String getLogGroupName() { return "dummy-log-group"; }
        @Override public String getLogStreamName() { return "dummy-log-stream"; }
        @Override public String getFunctionName() { return "dummy-function"; }
        @Override public String getFunctionVersion() { return "dummy-version"; }
        @Override public String getInvokedFunctionArn() { return "dummy-arn"; }
        @Override public CognitoIdentity getIdentity() { return null; }
        @Override public ClientContext getClientContext() { return null; }
        @Override public int getRemainingTimeInMillis() { return 300000; }
        @Override public int getMemoryLimitInMB() { return 512; }
        @Override
        public LambdaLogger getLogger() {
            return new LambdaLogger() {
                @Override
                public void log(String message) {
                    System.out.println(message);
                }
                @Override
                public void log(byte[] bytes) {
                    System.out.println(new String(bytes));
                }
            };
        }
    }

    @Test
    public void testListMultipleListingsForUser() {
        Context context = new DummyContext();

        // Prepare common request context with Cognito claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "integration@test.com");
        Map<String, Object> authorizer = new HashMap<>();
        authorizer.put("claims", claims);
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        proxyContext.setAuthorizer(authorizer);

        // --- Create first Listing (POST) ---
        APIGatewayProxyRequestEvent createEvent1 = new APIGatewayProxyRequestEvent();
        createEvent1.setHttpMethod("POST");
        createEvent1.setBody("{\"name\":\"First Listing\", \"attributes\":{\"color\":\"red\"}}");
        createEvent1.setRequestContext(proxyContext);
        APIGatewayProxyResponseEvent createResponse1 = lambda.handleRequest(createEvent1, context);
        String locationHeader1 = createResponse1.getHeaders().get("Location");
        String listingId1 = locationHeader1.substring(locationHeader1.lastIndexOf("/") + 1);
        assertFalse(listingId1.isEmpty(), "First Listing ID should be extracted");

        // --- Create second Listing (POST) ---
        APIGatewayProxyRequestEvent createEvent2 = new APIGatewayProxyRequestEvent();
        createEvent2.setHttpMethod("POST");
        createEvent2.setBody("{\"name\":\"Second Listing\", \"attributes\":{\"color\":\"green\"}}");
        createEvent2.setRequestContext(proxyContext);
        APIGatewayProxyResponseEvent createResponse2 = lambda.handleRequest(createEvent2, context);
        String locationHeader2 = createResponse2.getHeaders().get("Location");
        String listingId2 = locationHeader2.substring(locationHeader2.lastIndexOf("/") + 1);
        assertFalse(listingId2.isEmpty(), "Second Listing ID should be extracted");

        // --- List all Listings (GET without listingId) ---
        APIGatewayProxyRequestEvent listEvent = new APIGatewayProxyRequestEvent();
        listEvent.setHttpMethod("GET");
        listEvent.setPathParameters(null); // to trigger the list branch
        listEvent.setRequestContext(proxyContext);

        // Delay for the DB' GSI to update.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        APIGatewayProxyResponseEvent listResponse = lambda.handleRequest(listEvent, context);
        assertEquals(200, listResponse.getStatusCode(), "Expected HTTP 200 for list operation");
        String listBody = listResponse.getBody();
        assertNotNull(listBody, "List response body should not be null");
        // Check that both listing IDs are present in the response
        assertTrue(listBody.contains(listingId1), "List should include the first listing ID");
        assertTrue(listBody.contains(listingId2), "List should include the second listing ID");
    }

}
