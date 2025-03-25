package it.tref.dynamicpricing.aws.lambda.client;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import it.tref.dynamicpricing.aws.lambda.config.ConfigService;

/**
 * Service for creating and providing a DynamoDB client.
 * <p>
 * This class builds a {@link DynamoDbClient} using configuration from the {@link ConfigService},
 * such as the AWS region.
 * </p>
 */
@ApplicationScoped
public class DynamoDBClientService {

    private final DynamoDbClient dynamoDbClient;

    /**
     * Constructs a new DynamoDBClientService.
     * <p>
     * The client is built using the AWS region provided by the {@link ConfigService}.
     * </p>
     *
     * @param configService the configuration service that provides DynamoDB settings.
     */
    public DynamoDBClientService(ConfigService configService) {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(configService.getDynamoDbRegion()))
                .build();
    }

    /**
     * Returns the {@link DynamoDbClient} instance.
     *
     * @return the DynamoDbClient instance.
     */
    public DynamoDbClient getDynamoDbClient() {
        return dynamoDbClient;
    }

    /**
     * Closes the DynamoDbClient when the bean is destroyed.
     */
    @PreDestroy
    public void close() {
        if (dynamoDbClient != null) {
            dynamoDbClient.close();
        }
    }
}
