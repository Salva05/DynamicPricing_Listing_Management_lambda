package it.tref.dynamicpricing.aws.lambda.repository;

import it.tref.dynamicpricing.aws.lambda.aop.DynamoDBErrorHandled;
import it.tref.dynamicpricing.aws.lambda.config.ConfigService;
import it.tref.dynamicpricing.aws.lambda.mapper.DynamoDBListingMapper;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ListingRepository using AWS DynamoDB configured client.
 */
@DynamoDBErrorHandled
@ApplicationScoped
public class DynamoDBListingRepository implements ListingRepository {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBListingRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final ConfigService configService;
    private final DynamoDBListingMapper dynamoDBListingMapper;

    /**
     * Constructs a new DynamoDBListingRepository.
     *
     * @param dynamoDbClient        the DynamoDB client.
     * @param configService         the configuration service.
     * @param dynamoDBListingMapper the mapper to convert Listing objects to DynamoDB items.
     */
    public DynamoDBListingRepository(DynamoDbClient dynamoDbClient,
                                     ConfigService configService,
                                     DynamoDBListingMapper dynamoDBListingMapper) {
        this.dynamoDbClient = dynamoDbClient;
        this.configService = configService;
        this.dynamoDBListingMapper = dynamoDBListingMapper;
    }

    /**
     * Builds a composite key for a DynamoDB item using the provided listing ID and user ID.
     *
     * @param listingId the unique identifier for the listing.
     * @param userId    the unique identifier for the user.
     * @return a map representing the composite key with "listingId" and "userId" as keys.
     */
    private Map<String, AttributeValue> buildCompositeKey(String listingId, String userId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("listingId", AttributeValue.builder().s(listingId).build());
        key.put("userId", AttributeValue.builder().s(userId).build());
        return key;
    }

    /**
     * Retrieves a Listing from DynamoDB using its composite primary key.
     *
     * @param listingId the unique identifier for the listing.
     * @param userId    the unique identifier for the user.
     * @return the Listing if found, or null if not found.
     */
    @Override
    public Listing findById(String listingId, String userId) {
        Map<String, AttributeValue> key = buildCompositeKey(listingId, userId);
        GetItemRequest request = GetItemRequest.builder()
                .tableName(configService.getDynamoDbListingTableName())
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);
        if (response.hasItem() && !response.item().isEmpty()) {
            return dynamoDBListingMapper.fromDynamoDbItem(response.item());
        }
        return null;
    }

    /**
     * Persists the given Listing in DynamoDB.
     *
     * @param listing the listing to persist.
     */
    @Override
    public void save(Listing listing) {
        Map<String, AttributeValue> item = dynamoDBListingMapper.toDynamoDbItem(listing);
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(configService.getDynamoDbListingTableName())
                .item(item)
                .build();
        dynamoDbClient.putItem(putItemRequest);
        logger.info("Successfully persisted listing with ID: {}", listing.getListingId());
    }

    /**
     * Updates an existing listing in DynamoDB using the UpdateItem API.
     *
     * @param listing the Listing object containing updated data. The listing must have a valid listingId and userId.
     * @throws RuntimeException if the update operation fails.
     */
    @Override
    public void update(Listing listing) {
        Map<String, AttributeValue> key = buildCompositeKey(listing.getListingId(), listing.getUserId());
        String updateExpression = "SET #name = :name, #attributes = :attributes";
        Map<String, String> exprAttrNames = Map.of(
                "#name", "name",
                "#attributes", "attributes"
        );
        Map<String, AttributeValue> exprAttrValues = new HashMap<>();
        exprAttrValues.put(":name", AttributeValue.builder().s(listing.getName()).build()); // Name binding
        Map<String, AttributeValue> attributesMap = listing.getAttributes().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> AttributeValue.builder().s(e.getValue().toString()).build()
                ));
        exprAttrValues.put(":attributes", AttributeValue.builder().m(attributesMap).build()); // Attributes binding

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(configService.getDynamoDbListingTableName())
                .key(key)
                .updateExpression(updateExpression)
                .expressionAttributeNames(exprAttrNames)
                .expressionAttributeValues(exprAttrValues)
                .build();

        dynamoDbClient.updateItem(request);
        logger.info("Updated listing with ID: {} for user: {}", listing.getListingId(), listing.getUserId());
    }

    /**
     * Retrieves all listings associated with the specified user ID from DynamoDB using the Global Secondary Index (GSI).
     *
     * @param userId the unique identifier for the user.
     * @return a list of Listing objects for the given user, or an empty list if no listings are found.
     */
    @Override
    public List<Listing> findByUserId(String userId) {
        Map<String, AttributeValue> expressionAttributeValues = Map.of(
                ":userId", AttributeValue.builder().s(userId).build()
        );

        // Query using the GSI
        QueryRequest request = QueryRequest.builder()
                .tableName(configService.getDynamoDbListingTableName())
                .indexName(configService.getDynamoDbUserListingsIndexName())
                .keyConditionExpression("userId = :userId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        QueryResponse response = dynamoDbClient.query(request);
        logger.info("Found {} listings for user {}", response.count(), userId);

        return response.items().stream()
                .map(dynamoDBListingMapper::fromDynamoDbItem)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a Listing from DynamoDB identified by its composite primary key.
     *
     * @param listingId the unique identifier for the listing.
     * @param userId    the unique identifier for the user.
     */
    @Override
    public void delete(String listingId, String userId) {
        Map<String, AttributeValue> key = buildCompositeKey(listingId, userId);
        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(configService.getDynamoDbListingTableName())
                .key(key)
                .build();
        dynamoDbClient.deleteItem(request);
        logger.info("Deleted listing with ID: {} for user: {}", listingId, userId);
    }
}
