package it.tref.dynamicpricing.aws.lambda.repository;

import it.tref.dynamicpricing.aws.lambda.config.ConfigService;
import it.tref.dynamicpricing.aws.lambda.mapper.DynamoDBListingMapper;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.Map;

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
}
