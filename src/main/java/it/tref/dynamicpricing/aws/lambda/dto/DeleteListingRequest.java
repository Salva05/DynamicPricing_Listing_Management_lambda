package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

/**
 * Represents the request payload for deleting a listing.
 * <p>
 * The deletion request requires the {@code listingId} to identify the listing.
 * The {@code userId} is extracted from token claims and is therefore not included.
 * </p>
 */
public class DeleteListingRequest {

    @NotEmpty(message = "Listing ID is required")
    @JsonProperty("listingId")
    private String listingId;

    @JsonCreator
    public DeleteListingRequest() {
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    @Override
    public String toString() {
        return "DeleteListingRequest{" +
                "listingId='" + listingId + '\'' +
                '}';
    }
}
