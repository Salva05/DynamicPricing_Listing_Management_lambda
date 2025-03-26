package it.tref.dynamicpricing.aws.lambda.client;

import it.tref.dynamicpricing.aws.lambda.config.ConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBClientServiceTest {

    @Test
    public void testDynamoDBClientServiceInitialization() {
        // Create a stub ConfigService returning a mock region.
        ConfigService configService = new ConfigService() {
            @Override
            public String getDynamoDbRegion() {
                return "eu-west-1";
            }
        };

        // Create the service.
        DynamoDBClientService service = new DynamoDBClientService(configService);
        DynamoDbClient client = service.getDynamoDbClient();

        // Assert that the client is not null.
        Assertions.assertNotNull(client);
    }
}
