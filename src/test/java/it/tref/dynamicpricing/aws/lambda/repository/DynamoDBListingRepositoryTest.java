package it.tref.dynamicpricing.aws.lambda.repository;

import it.tref.dynamicpricing.aws.lambda.config.ConfigService;
import it.tref.dynamicpricing.aws.lambda.mapper.DynamoDBListingMapper;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DynamoDBListingRepositoryTest {

    private DynamoDbClient dynamoDbClient;
    private ConfigService configService;
    private DynamoDBListingMapper dynamoDBListingMapper;
    private ListingRepository listingRepository;

    @BeforeEach
    public void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        configService = mock(ConfigService.class);
        dynamoDBListingMapper = mock(DynamoDBListingMapper.class);
        listingRepository = new DynamoDBListingRepository(dynamoDbClient, configService, dynamoDBListingMapper);
    }

    @Test
    public void testSaveListing() {
        Listing listing = new Listing();
        listing.setListingId("test-id");
        listing.setName("Test Listing");

        Map<String, AttributeValue> dummyItem = Collections.singletonMap("listingId", AttributeValue.builder().s("test-id").build());
        when(dynamoDBListingMapper.toDynamoDbItem(listing)).thenReturn(dummyItem);
        when(configService.getDynamoDbListingTableName()).thenReturn("TestTable");
        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());

        listingRepository.save(listing);

        ArgumentCaptor<PutItemRequest> requestCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient, times(1)).putItem(requestCaptor.capture());
        PutItemRequest capturedRequest = requestCaptor.getValue();
        assertEquals("TestTable", capturedRequest.tableName());
        assertEquals(dummyItem, capturedRequest.item());
    }

    @Test
    public void testUpdateDynamoDbExceptionHandling() {
        // Create a sample listing
        Listing listing = new Listing();
        listing.setListingId("testId");
        listing.setUserId("user@example.com");
        listing.setName("Test Listing");

        // Prepare a composite key
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("listingId", AttributeValue.builder().s("testId").build());
        key.put("userId", AttributeValue.builder().s("user@example.com").build());

        // When building the DynamoDB item from the listing, return a dummy map.
        Map<String, AttributeValue> dummyItem = Collections.singletonMap("dummyKey", AttributeValue.builder().s("dummyValue").build());
        when(dynamoDBListingMapper.toDynamoDbItem(listing)).thenReturn(dummyItem);
        when(configService.getDynamoDbListingTableName()).thenReturn("TestTable");

        // Simulate a DynamoDB error when calling updateItem.
        RuntimeException dynamoException = new RuntimeException("DynamoDB error");
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenThrow(dynamoException);

        Exception thrown = assertThrows(RuntimeException.class, () ->
                listingRepository.update(listing));
        assertEquals("DynamoDB error", thrown.getMessage());
    }

    @Test
    public void testUpdateListingSuccess() {
        // Prepare an existing listing
        Listing listing = new Listing();
        listing.setListingId("testId");
        listing.setUserId("user@example.com");
        listing.setName("Original Name");
        listing.addAttribute("color", "red");

        // Configure the config service
        when(configService.getDynamoDbListingTableName()).thenReturn("TestTable");

        // Simulate a successful update
        UpdateItemResponse updateResponse = UpdateItemResponse.builder().build();
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(updateResponse);

        // Call update
        listingRepository.update(listing);

        // Capture and verify the UpdateItemRequest
        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(dynamoDbClient).updateItem(captor.capture());
        UpdateItemRequest capturedRequest = captor.getValue();

        // Verify table name and key.
        assertEquals("TestTable", capturedRequest.tableName());
        Map<String, AttributeValue> key = capturedRequest.key();
        assertEquals("testId", key.get("listingId").s());
        assertEquals("user@example.com", key.get("userId").s());

        // Optionally, verify update expression and attribute values.
        assertNotNull(capturedRequest.updateExpression());
        assertTrue(capturedRequest.expressionAttributeNames().containsKey("#name"));
        assertTrue(capturedRequest.expressionAttributeValues().containsKey(":name"));
    }

    @Test
    public void testFindByIdFound() {
        // Prepare a dummy DynamoDB item
        Map<String, AttributeValue> dummyItem = new HashMap<>();
        dummyItem.put("listingId", AttributeValue.builder().s("testId").build());
        dummyItem.put("userId", AttributeValue.builder().s("user@example.com").build());
        dummyItem.put("name", AttributeValue.builder().s("Appartamento vista mare").build());

        // Configure the config service
        when(configService.getDynamoDbListingTableName()).thenReturn("TestTable");
        // Simulate a GetItemResponse with the dummy item
        GetItemResponse getResponse = GetItemResponse.builder().item(dummyItem).build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(getResponse);

        // Simulate the mapper converting the DynamoDB item into a Listing.
        Listing expectedListing = new Listing();
        expectedListing.setListingId("testId");
        expectedListing.setUserId("user@example.com");
        expectedListing.setName("Appartamento vista mare");
        when(dynamoDBListingMapper.fromDynamoDbItem(dummyItem)).thenReturn(expectedListing);

        // Call findById
        Listing result = listingRepository.findById("testId", "user@example.com");

        // Verify that the listing is found and properly mapped
        assertNotNull(result);
        assertEquals("testId", result.getListingId());
        assertEquals("user@example.com", result.getUserId());
        assertEquals("Appartamento vista mare", result.getName());
    }

    @Test
    public void testFindByIdNotFound() {
        // Configure the config service
        when(configService.getDynamoDbListingTableName()).thenReturn("TestTable");
        // Simulate a GetItemResponse with an empty item map
        GetItemResponse getResponse = GetItemResponse.builder().item(Collections.emptyMap()).build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(getResponse);

        // Call findById
        Listing result = listingRepository.findById("nonexistent", "user@example.com");

        // Verify that no listing is returned
        assertNull(result);
    }

    @Test
    public void testFindByUserIdFound() {
        // Prepare two dummy DynamoDB items.
        Map<String, AttributeValue> item1 = new HashMap<>();
        item1.put("listingId", AttributeValue.builder().s("listing-1").build());
        item1.put("userId", AttributeValue.builder().s("user@example.com").build());
        item1.put("name", AttributeValue.builder().s("Listing One").build());

        Map<String, AttributeValue> item2 = new HashMap<>();
        item2.put("listingId", AttributeValue.builder().s("listing-2").build());
        item2.put("userId", AttributeValue.builder().s("user@example.com").build());
        item2.put("name", AttributeValue.builder().s("Listing Two").build());

        // Simulate QueryResponse with these two items.
        QueryResponse queryResponse = QueryResponse.builder()
                .items(Arrays.asList(item1, item2))
                .count(2)
                .build();

        when(configService.getDynamoDbListingTableName()).thenReturn("TestTable");
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);

        // Simulate mapper conversion for both items.
        Listing listing1 = new Listing();
        listing1.setListingId("listing-1");
        listing1.setUserId("user@example.com");
        listing1.setName("Listing One");

        Listing listing2 = new Listing();
        listing2.setListingId("listing-2");
        listing2.setUserId("user@example.com");
        listing2.setName("Listing Two");

        when(dynamoDBListingMapper.fromDynamoDbItem(item1)).thenReturn(listing1);
        when(dynamoDBListingMapper.fromDynamoDbItem(item2)).thenReturn(listing2);

        // Call findByUserId.
        List<Listing> result = listingRepository.findByUserId("user@example.com");

        // Verify that both listings are returned and correctly mapped.
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Listing One", result.get(0).getName());
        assertEquals("Listing Two", result.get(1).getName());
    }

    @Test
    public void testFindByUserIdEmpty() {
        // Simulate QueryResponse with no items.
        QueryResponse queryResponse = QueryResponse.builder()
                .items(Collections.emptyList())
                .count(0)
                .build();

        when(configService.getDynamoDbListingTableName()).thenReturn("TestTable");
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);

        // Call findByUserId.
        List<Listing> result = listingRepository.findByUserId("user@example.com");

        // Verify that the result is an empty list.
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
