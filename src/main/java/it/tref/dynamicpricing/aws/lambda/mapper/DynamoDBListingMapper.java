package it.tref.dynamicpricing.aws.lambda.mapper;

import it.tref.dynamicpricing.aws.lambda.model.Listing;
import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     *   <li>{@code prediction} (Object)</li>
     * </ul>
     * <p>
     * Any dynamic attributes present in the Listing are nested under the key "attributes" as a map of strings.
     * </p>
     *
     * @param listing the {@link Listing} object to convert.
     * @return a {@code Map<String, AttributeValue>} representing the DynamoDB item.
     */
    public Map<String, AttributeValue> toDynamoDbItem(Listing listing) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("listingId", AttributeValue.builder().s(listing.getListingId()).build());
        item.put("userId", AttributeValue.builder().s(listing.getUserId()).build());
        item.put("createdAt", AttributeValue.builder().s(listing.getCreatedAt().toString()).build());
        item.put("completed", AttributeValue.builder().bool(listing.isCompleted()).build());
        item.put("name", AttributeValue.builder().s(listing.getName()).build());

        // Nest dynamic attributes into the key "attributes" as strings
        if (!listing.getAttributes().isEmpty()) {
            Map<String, AttributeValue> attributesMap = new HashMap<>();
            listing.getAttributes().forEach((k, v) -> {
                if (v != null) {
                    if (v instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) v;
                        // Convert each item to an AttributeValue (as a string)
                        attributesMap.put(k, AttributeValue.builder()
                                .l(list.stream()
                                        .map(item_ -> AttributeValue.builder().s(item_.toString()).build())
                                        .collect(java.util.stream.Collectors.toList()))
                                .build());
                    } else {
                        attributesMap.put(k, AttributeValue.builder().s(v.toString()).build());
                    }
                }
            });
            item.put("attributes", AttributeValue.builder().m(attributesMap).build());
        }

        if (listing.getPrediction() != null) {
            Map<String, Object> predictionObj = (Map<String, Object>) listing.getPrediction();
            Map<String, AttributeValue> predictionMap = new HashMap<>();
            predictionObj.forEach((k, v) -> {
                if (v instanceof Map) {
                    Map<String, Object> innerMap = (Map<String, Object>) v;
                    Map<String, AttributeValue> innerAttrMap = new HashMap<>();
                    innerMap.forEach((innerKey, innerVal) -> {
                        if (innerVal != null) {
                            innerAttrMap.put(innerKey, AttributeValue.builder().s(innerVal.toString()).build());
                        }
                    });
                    predictionMap.put(k, AttributeValue.builder().m(innerAttrMap).build());
                } else if (v != null) {
                    predictionMap.put(k, AttributeValue.builder().s(v.toString()).build());
                }
            });
            item.put("prediction", AttributeValue.builder().m(predictionMap).build());
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
     *   <li>{@code prediction} (Object)</li>
     * </ul>
     * <p>
     * If present, dynamic attributes are retrieved from the nested "attributes" map and added to the Listing's attributes.
     * </p>
     *
     * @param item the DynamoDB item map to convert.
     * @return the corresponding {@link Listing} object.
     */
    public Listing fromDynamoDbItem(Map<String, AttributeValue> item) {
        Listing listing = new Listing();

        // Required attributes
        listing.setListingId(item.get("listingId").s());
        listing.setUserId(item.get("userId").s());

        // ISO-8601 format required
        listing.setCreatedAt(Instant.parse(item.get("createdAt").s()));
        listing.setName(item.get("name").s());
        listing.setCompleted(item.get("completed").bool());

        if (item.containsKey("attributes") && item.get("attributes").m() != null) {
            Map<String, AttributeValue> attributesMap = item.get("attributes").m();
            attributesMap.forEach((k, v) -> {
                if (v.l() != null && !v.l().isEmpty()) {
                    // Convert list of AttributeValues to a List of strings
                    listing.getAttributes().put(k, v.l().stream()
                            .map(AttributeValue::s)
                            .filter(Objects::nonNull)
                            .collect(java.util.stream.Collectors.toList()));
                } else if (v.s() != null) {
                    listing.getAttributes().put(k, v.s());
                }
            });
        }

        if (item.containsKey("prediction") && item.get("prediction").m() != null) {
            Map<String, AttributeValue> predictionAttrMap = item.get("prediction").m();
            Map<String, Object> prediction = new HashMap<>();
            predictionAttrMap.forEach((key, attrVal) -> {
                if (attrVal.m() != null) {
                    Map<String, AttributeValue> innerMap = attrVal.m();
                    Map<String, Object> innerPrediction = new HashMap<>();
                    innerMap.forEach((innerKey, innerAttrVal) -> {
                        if (innerAttrVal.s() != null) {
                            innerPrediction.put(innerKey, innerAttrVal.s());
                        }
                    });
                    prediction.put(key, innerPrediction);
                } else if (attrVal.s() != null) {
                    prediction.put(key, attrVal.s());
                }
            });
            listing.setPrediction(prediction);
        }

        return listing;
    }

}

