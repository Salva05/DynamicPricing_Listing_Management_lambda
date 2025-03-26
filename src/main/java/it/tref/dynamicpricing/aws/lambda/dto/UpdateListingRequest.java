package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * Represents the request payload for updating an existing listing.
 * <p>
 * This DTO includes only the fields that the client is allowed to modify.
 * </p>
 */
public class UpdateListingRequest {

    @NotEmpty(message = "Listing ID is required")
    @JsonProperty(value = "listingId", required = true)
    private String listingId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    @JsonCreator
    public UpdateListingRequest() {
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "UpdateListingDTO{" +
                "listingId='" + listingId + '\'' +
                ", name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
