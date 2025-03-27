package it.tref.dynamicpricing.aws.lambda.exception;

/**
 * Thrown when JSON deserialization or serialization fails.
 */
public class JsonProcessingRuntimeException extends RuntimeException {
    public JsonProcessingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
