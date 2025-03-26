package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response payload after successfully creating a listing.
 * <p>
 * It contains the unique identifier for the newly created listing.
 * </p>
 */
public class CreateListingResponse {

    @JsonProperty("listingId")
    private String listingId;

    @JsonCreator
    public CreateListingResponse() {
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    @Override
    public String toString() {
        return "CreateListingResponse{" +
                "listingId='" + listingId + '\'' +
                '}';
    }
}
