package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import java.util.List;

/**
 * Represents the response payload for listing multiple listings.
 * <p>
 * The payload contains a list of all listings owned by the user.
 * </p>
 */
@RegisterForReflection
public class ListListingsResponse {

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

    /**
     * Returns the listings list for JSON serialization as a top-level array.
     */
    @JsonValue
    public List<Listing> value() {
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
