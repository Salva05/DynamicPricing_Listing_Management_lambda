package it.tref.dynamicpricing.aws.lambda.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MapperService provides helper methods for JSON serialization and deserialization
 * using Jackson's {@link ObjectMapper}.
 * <p>
 * The service is configured to ignore unknown properties during deserialization,
 * ensuring that JSON input with additional unexpected fields does not cause errors.
 * </p>
 */
@ApplicationScoped
public class MapperService {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs a new MapperService and configures the {@link ObjectMapper}.
     */
    public MapperService() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Deserializes the provided JSON string into an object of the specified type.
     *
     * @param <T> the type of the object to deserialize into.
     * @param value the JSON string to be deserialized.
     * @param valueType the {@link Class} of the type T.
     * @return an instance of type T populated with data from the JSON string.
     * @throws JsonProcessingException if there is an error during deserialization.
     */
    public <T> T readValue(String value, Class<T> valueType) throws JsonProcessingException {
        return mapper.readValue(value, valueType);
    }

    /**
     * Serializes the provided object into a JSON string.
     *
     * @param value the object to be serialized.
     * @return a JSON string representation of the object.
     * @throws JsonProcessingException if there is an error during serialization.
     */
    public String writeValueAsString(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }
}
