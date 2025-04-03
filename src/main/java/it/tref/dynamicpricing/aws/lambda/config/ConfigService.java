package it.tref.dynamicpricing.aws.lambda.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Provides configuration properties for the application.
 */
@ApplicationScoped
public class ConfigService {

    /**
     * The AWS region for DynamoDB.
     * <p>
     * If not explicitly set, it defaults to "eu-south-1".
     * </p>
     */
    @ConfigProperty(name = "REGION", defaultValue = "eu-south-1")
    String dynamoDbRegion;

    /**
     * The AWS region for SQS Queue.
     * <p>
     * If not explicitly set, it defaults to "eu-south-1".
     * </p>
     */
    @ConfigProperty(name = "REGION", defaultValue = "eu-south-1")
    String sqsQueueRegion;

    /**
     * The SQS queue URL.
     */
    @ConfigProperty(name = "PREDICTION_QUEUE_URL")
    public String sqsQueueUrl;

    /**
     * Domain URL of the frontend
     */
    @ConfigProperty(name = "DOMAIN_URL", defaultValue = "https://dnyas0faoobat.cloudfront.net")
    public String domainUrl;

    /**
     * The DynamoDB listing table name.
     */
    @ConfigProperty(name = "LISTING_TABLE_NAME")
    String dynamoDbListingTableName;

    /**
     * The DynamoDB Global Secondary Index name for User's Listings.
     */
    @ConfigProperty(name = "LISTING_INDEX_TABLE_NAME")
    String dynamoDbUserListingsIndexName;

    public String getDynamoDbRegion() {
        return dynamoDbRegion;
    }

    public String getSqsQueueRegion() {
        return sqsQueueRegion;
    }

    public String getSqsQueueUrl() {
        return sqsQueueUrl;
    }

    public String getDynamoDbListingTableName() {
        return dynamoDbListingTableName;
    }
    public String getDomainUrl() { return domainUrl; }

    public String getDynamoDbUserListingsIndexName() {
        return dynamoDbUserListingsIndexName;
    }

}
