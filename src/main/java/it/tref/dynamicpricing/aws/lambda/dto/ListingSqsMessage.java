package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the message payload for the SQS queue.
 * <p>
 * This payload includes the composite key of the listing (listingId and userId)
 * along with the listing details (attributes) required for processing.
 * </p>
 */
@RegisterForReflection
public class ListingSqsMessage {

    @JsonProperty("listingId")
    private String listingId;

    @JsonProperty("userId")
    private String userId;

    /**
     * Represents the listing details (attributes) for the SQS message.
     */
    @JsonProperty("listing_details")
    private Map<String, String> listingDetails;

    public ListingSqsMessage() {
    }

    /**
     * Constructs a new ListingSqsMessage with the specified composite key and listing details.
     *
     * @param listingId      the unique identifier of the listing.
     * @param userId         the unique identifier of the user.
     * @param listingDetails the attributes of the listing.
     */
    @JsonCreator
    public ListingSqsMessage(@JsonProperty("listingId") String listingId,
                             @JsonProperty("userId") String userId,
                             @JsonProperty("listing_details") Map<String, String> listingDetails) {
        this.listingId = listingId;
        this.userId = userId;
        this.listingDetails = listingDetails;
    }

    /**
     * Converts a map of attributes from {@code Map<String, Object>} to {@code Map<String, String>}.
     * <p>
     * This method iterates over the provided attributes map and converts each value to its string representation.
     * </p>
     *
     * @param attributes the original attributes map with Object values.
     * @return a new map where each value is converted to a String.
     */
    public static Map<String, String> convertAttributes(Map<String, Object> attributes) {
        return attributes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, String> getListingDetails() {
        return listingDetails;
    }

    public void setListingDetails(Map<String, String> listingDetails) {
        this.listingDetails = listingDetails;
    }

    @Override
    public String toString() {
        return "ListingSqsMessage{" +
                "listingId='" + listingId + '\'' +
                ", userId='" + userId + '\'' +
                ", listingDetails=" + listingDetails +
                '}';
    }
}
