package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * Represents the request payload for updating an existing listing.
 * <p>
 * This DTO includes only the fields that the client is allowed to modify.
 * The unique identifier (listingId) is provided as
 * a URL path parameter (e.g. PUT /listings/{listingId}).
 * </p>
 */
@RegisterForReflection
public class UpdateListingRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    public UpdateListingRequest() {
    }

    @JsonCreator
    public UpdateListingRequest(@JsonProperty("name") String name,
                                @JsonProperty("attributes") Map<String, Object> attributes) {
        this.name = name;
        this.attributes = attributes;
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
        return "UpdateListingRequest{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
