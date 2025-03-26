package it.tref.dynamicpricing.aws.lambda.mapper;

import it.tref.dynamicpricing.aws.lambda.model.Listing;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MapperServiceTest {

    @Inject
    MapperService mapperService;

    @Test
    public void testSerializationDeserialization() throws Exception {
        // Create a Listing instance with test data.
        Listing listing = new Listing();
        listing.setListingId("testId");
        listing.setUserId("user123");
        listing.addAttribute("key", "value");

        // Serialize Listing to JSON.
        String json = mapperService.writeValueAsString(listing);
        // Deserialize back to Listing.
        Listing fromJson = mapperService.readValue(json, Listing.class);

        // Assert that properties match.
        Assertions.assertEquals(listing.getListingId(), fromJson.getListingId());
        Assertions.assertEquals(listing.getUserId(), fromJson.getUserId());
        Assertions.assertEquals(listing.getAttributes(), fromJson.getAttributes());
    }
}
