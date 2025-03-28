package it.tref.dynamicpricing.aws.lambda.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.tref.dynamicpricing.aws.lambda.handler.CreateListingHandler;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * Represents the request payload for creating a new listing.
 * <p>
 * This DTO only includes the fields the client is allowed to provide.
 * </p>
 */
public class CreateListingRequest {

    @NotEmpty(message = "Listing name is required")
    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    public CreateListingRequest() {
    }

    @JsonCreator
    public CreateListingRequest(
            @JsonProperty(value = "name", required = true) String name,
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
        return "CreateListingRequest{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
