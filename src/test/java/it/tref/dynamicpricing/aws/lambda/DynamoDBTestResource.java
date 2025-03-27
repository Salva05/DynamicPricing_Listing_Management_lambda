package it.tref.dynamicpricing.aws.lambda;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.Map;

public class DynamoDBTestResource implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> dynamoDBContainer;

    @Override
    public Map<String, String> start() {
        // Dummy credentials for AWS SDK
        System.setProperty("aws.accessKeyId", "dummy");
        System.setProperty("aws.secretAccessKey", "dummy");

        // Start DynamoDB Local on fixed port 8000
        dynamoDBContainer = new GenericContainer<>("amazon/dynamodb-local:latest")
                .withExposedPorts(8000);
        dynamoDBContainer.start();

        String endpoint = "http://" + dynamoDBContainer.getHost() + ":" + dynamoDBContainer.getMappedPort(8000);

        try (DynamoDbClient client = DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("eu-south-1"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy"))
                )
                .build()) {

            String tableName = "dynamic-pricing-demo-listings";
            String gsiName = "dynamic-pricing-demo-listings-users-index";

            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(tableName)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("listingId")
                                    .keyType(KeyType.HASH)
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName("userId")
                                    .keyType(KeyType.RANGE)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("listingId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("userId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .globalSecondaryIndexes(
                            GlobalSecondaryIndex.builder()
                                    .indexName(gsiName)
                                    .keySchema(
                                            KeySchemaElement.builder()
                                                    .attributeName("userId")
                                                    .keyType(KeyType.HASH)
                                                    .build(),
                                            KeySchemaElement.builder()
                                                    .attributeName("listingId")
                                                    .keyType(KeyType.RANGE)
                                                    .build()
                                    )
                                    .projection(Projection.builder()
                                            .projectionType(ProjectionType.ALL)
                                            .build())
                                    .build()
                    )
                    .build();

            try {
                client.createTable(createTableRequest);
            } catch (ResourceInUseException e) {
                // Table already exists
            }

            // Poll until the table is active
            DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();
            int attempts = 0;
            while (attempts < 10) {
                TableDescription tableDescription = client.describeTable(describeTableRequest).table();
                if (TableStatus.ACTIVE.equals(tableDescription.tableStatus())) {
                    break;
                }
                Thread.sleep(1000);
                attempts++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for table to become active", e);
        }
        return Map.of("quarkus.dynamodb.endpoint-override", endpoint);
    }

    @Override
    public void stop() {
        if (dynamoDBContainer != null) {
            dynamoDBContainer.stop();
        }
    }
}
