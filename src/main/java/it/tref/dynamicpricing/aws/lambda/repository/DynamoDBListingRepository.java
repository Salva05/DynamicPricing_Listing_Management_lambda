package it.tref.dynamicpricing.aws.lambda.repository;

import it.tref.dynamicpricing.aws.lambda.config.ConfigService;
import it.tref.dynamicpricing.aws.lambda.mapper.DynamoDBListingMapper;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * Implementation of ListingRepository using AWS DynamoDB configured client.
 */
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
        try {
            dynamoDbClient.putItem(putItemRequest);
            logger.info("Successfully persisted listing with ID: {}", listing.getListingId());
        } catch (Exception e) {
            logger.error("Error persisting listing with ID: {}. Error: {}", listing.getListingId(), e.getMessage(), e);
            throw e;
        }
    }
}
