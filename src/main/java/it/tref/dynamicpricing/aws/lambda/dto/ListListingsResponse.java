package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import java.util.List;

/**
 * Represents the response payload for listing multiple listings.
 * <p>
 * The payload contains a list of all listings owned by the user.
 * </p>
 */
public class ListListingsResponse {

    @JsonProperty("listings")
    private List<Listing> listings;

    public ListListingsResponse() {
    }

    @JsonCreator
    public ListListingsResponse(@JsonProperty("listings") List<Listing> listings) {
        this.listings = listings;
    }

    public List<Listing> getListings() {
        return listings;
    }

    public void setListings(List<Listing> listings) {
        this.listings = listings;
    }

    @Override
    public String toString() {
        return "ListListingsResponse{" +
                "listings=" + listings +
                '}';
    }
}
