package it.tref.dynamicpricing.aws.lambda.mapper;

import io.quarkus.test.junit.QuarkusTest;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@QuarkusTest
public class ListingMapperTest {

    @Inject
    DynamoDBListingMapper dynamoDBListingMapper;

    @Test
    public void testToDynamoDbItemAndBack() {
        // Create a Listing instance with test data.
        Listing listing = new Listing();
        listing.setListingId("testId");
        listing.setUserId("user123");
        listing.setCompleted(true);
        listing.addAttribute("color", "blue");
        listing.addAttribute("price", "100");

        // Convert Listing to DynamoDB item map.
        Map<String, AttributeValue> item = dynamoDBListingMapper.toDynamoDbItem(listing);

        // Convert back to Listing.
        Listing convertedListing = dynamoDBListingMapper.fromDynamoDbItem(item);

        // Assert that the fixed and dynamic properties match.
        Assertions.assertEquals(listing.getListingId(), convertedListing.getListingId());
        Assertions.assertEquals(listing.getUserId(), convertedListing.getUserId());
        Assertions.assertEquals(listing.isCompleted(), convertedListing.isCompleted());
        Assertions.assertEquals(listing.getAttributes(), convertedListing.getAttributes());
    }
}
