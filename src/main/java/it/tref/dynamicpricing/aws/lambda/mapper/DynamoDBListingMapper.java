package it.tref.dynamicpricing.aws.lambda.mapper;

import it.tref.dynamicpricing.aws.lambda.model.Listing;
import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper class for converting between {@link Listing} objects and DynamoDB item maps.
 * <p>
 * This class provides static helper methods to convert a Listing into a DynamoDB-compatible item map
 * and vice versa. Fixed properties such as {@code listingId}, {@code userId}, {@code createdAt}, and {@code completed}
 * are mapped to the root level, while dynamic attributes are nested under the "attributes" key.
 * </p>
 */
@ApplicationScoped
public class DynamoDBListingMapper {

    /**
     * Converts a {@link Listing} object to a DynamoDB item map.
     * <p>
     * The returned map includes fixed properties at the root:
     * <ul>
     *   <li>{@code listingId} (String)</li>
     *   <li>{@code userId} (String)</li>
     *   <li>{@code createdAt} (String in ISO-8601 format)</li>
     *   <li>{@code completed} (Boolean)</li>
     * </ul>
     * <p>
     * Any dynamic attributes present in the Listing are nested under the key "attributes" as a map of strings.
     * </p>
     *
     * @param listing the {@link Listing} object to convert.
     * @return a {@code Map<String, AttributeValue>} representing the DynamoDB item.
     */
    public static Map<String, AttributeValue> toDynamoDbItem(Listing listing) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("listingId", AttributeValue.builder().s(listing.getListingId()).build());
        item.put("userId", AttributeValue.builder().s(listing.getUserId()).build());
        item.put("createdAt", AttributeValue.builder().s(listing.getCreatedAt().toString()).build());
        item.put("completed", AttributeValue.builder().bool(listing.isCompleted()).build());

        // Nest dynamic attributes into the key "attributes" as strings
        if (!listing.getAttributes().isEmpty()) {
            Map<String, AttributeValue> attributesMap = new HashMap<>();
            listing.getAttributes().forEach((k, v) -> {
                if (v != null) {
                    attributesMap.put(k, AttributeValue.builder().s(v.toString()).build());
                }
            });
            item.put("attributes", AttributeValue.builder().m(attributesMap).build());
        }

        return item;
    }

    /**
     * Converts a DynamoDB item map into a {@link Listing} object.
     * <p>
     * This method extracts the fixed properties from the root of the map:
     * <ul>
     *   <li>{@code listingId} (String)</li>
     *   <li>{@code userId} (String)</li>
     *   <li>{@code createdAt} (String in ISO-8601 format, converted to {@link Instant})</li>
     *   <li>{@code completed} (Boolean)</li>
     * </ul>
     * <p>
     * If present, dynamic attributes are retrieved from the nested "attributes" map and added to the Listing's attributes.
     * </p>
     *
     * @param item the DynamoDB item map to convert.
     * @return the corresponding {@link Listing} object.
     */
    public static Listing fromDynamoDbItem(Map<String, AttributeValue> item) {
        Listing listing = new Listing();

        // Required attributes
        listing.setListingId(item.get("listingId").s());
        listing.setUserId(item.get("userId").s());

        // ISO-8601 format required
        listing.setCreatedAt(Instant.parse(item.get("createdAt").s()));

        listing.setCompleted(item.get("completed").bool());

        if (item.containsKey("attributes") && item.get("attributes").m() != null) {
            Map<String, AttributeValue> attributesMap = item.get("attributes").m();
            attributesMap.forEach((k, v) -> {
                if (v.s() != null) {
                    listing.getAttributes().put(k, v.s());
                }
            });
        }

        return listing;
    }

}

