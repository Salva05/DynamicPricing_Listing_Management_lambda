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
    @ConfigProperty(name = "quarkus.dynamodb.aws.region", defaultValue = "eu-south-1")
    String dynamoDbRegion;

    /**
     * The DynamoDB listing table name.
     */
    @ConfigProperty(name = "quarkus.dynamodb.listing.table.name")
    String dynamoDbListingTableName;

    /**
     * Returns the configured AWS region for DynamoDB.
     *
     * @return the AWS region as a {@link String}.
     */
    public String getDynamoDbRegion() {
        return dynamoDbRegion;
    }

    /**
     * Returns the DynamoDB listing table name.
     *
     * @return the DynamoDB listing table name.
     */
    public String getDynamoDbListingTableName() {
        return dynamoDbListingTableName;
    }


}
