package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.tref.dynamicpricing.aws.lambda.model.Listing;

/**
 * Represents the response payload for retrieving a listing.
 * <p>
 * The payload contains the complete details of the listing.
 * </p>
 */
@RegisterForReflection
public class GetListingResponse {

    private Listing listing;

    public GetListingResponse() {
    }

    @JsonCreator
    public GetListingResponse(@JsonProperty("listing") Listing listing) {
        this.listing = listing;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    @JsonValue
    public Listing value() {
        return listing;
    }

    @Override
    public String toString() {
        return "GetListingResponse{" +
                "listing=" + listing +
                '}';
    }
}
