package it.tref.dynamicpricing.aws.lambda.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a listing entity with fixed properties (listingId, userId, createdAt, completed)
 * and dynamic attributes that can be added at runtime.
 * <p>
 * The {@code createdAt} field is automatically set at instantiation, and the {@code completed} flag
 * indicates whether the AI price prediction has been performed.
 * </p>
 */
public class Listing {

    /**
     * Unique identifier for the listing.
     */
    @JsonProperty(value = "listingId", required = true)
    @NotEmpty(message = "Listing ID is required")
    private String listingId;

    /**
     * Identifier for the user that owns the listing.
     */
    @JsonProperty(value = "userId", required = true)
    @NotEmpty(message = "User ID is required")
    private String userId;

    /**
     * Timestamp when the listing was created.
     * <p>
     * This field is automatically set to the current time (in ISO-8601 format) when a new Listing is instantiated.
     * </p>
     */
    @JsonProperty(value = "createdAt", access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;

    /**
     * Indicates whether the AI price prediction has been completed.
     * <p>
     * This field is managed internally and is read-only from JSON input.
     * </p>
     */
    @JsonProperty(value = "completed", access = JsonProperty.Access.READ_ONLY)
    private boolean completed = false;

    /**
     * Holds additional dynamic attributes that are not explicitly defined as fields.
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * Default constructor that sets the creation timestamp.
     */
    public Listing() {
        this.createdAt = Instant.now();
    }

    /**
     * Returns the listing ID.
     *
     * @return the listing ID.
     */
    public String getListingId() {
        return listingId;
    }

    /**
     * Sets the listing ID.
     *
     * @param listingId the listing ID to set.
     */
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    /**
     * Returns the user ID.
     *
     * @return the user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation timestamp.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     * <p>
     * Note: Although this field is marked as read-only for JSON deserialization,
     * it can be updated internally (when converting from a DynamoDB item).
     * </p>
     *
     * @param createdAt the timestamp to set.
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Indicates whether the prediction has been completed.
     *
     * @return true if completed, false otherwise.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets the completed flag.
     *
     * @param completed the completed flag value.
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Returns a map of dynamic attributes.
     *
     * @return a map of dynamic attributes.
     */
    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Adds a dynamic attribute.
     *
     * @param key the attribute key.
     * @param value the attribute value.
     */
    @JsonAnySetter
    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Returns a string representation of the Listing.
     *
     * @return a string representation of the Listing.
     */
    @Override
    public String toString() {
        return "Listing{" +
                "listingId='" + listingId + '\'' +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                ", completed=" + completed +
                ", attributes=" + attributes +
                '}';
    }
}
