package it.tref.dynamicpricing.aws.lambda.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Utility class for extracting the userId (email) claim from an API Gateway event.
 * <p>
 * This class operates on the {@link APIGatewayProxyRequestEvent} by accessing its request context,
 * specifically the {@code authorizer} field, which is expected to contain a nested "claims" map.
 * The email claim, representing the user identifier, is extracted from this map.
 * </p>
 */
@ApplicationScoped
public class TokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);

    /**
     * Extracts the userId (email) from the API Gateway event's token claims.
     * <p>
     * Assumes that token claims are available in the request context.
     * </p>
     *
     * @param event the API Gateway request event.
     * @return the extracted userId.
     * @throws IllegalStateException if the userId cannot be extracted.
     */
    @SuppressWarnings("unchecked")
    public static String extractUserIdFromEvent(APIGatewayProxyRequestEvent event) {
        try {
            Map<String, Object> authorizer = (Map<String, Object>) event.getRequestContext().getAuthorizer();
            if (authorizer != null) {
                Map<String, Object> claims = (Map<String, Object>) authorizer.get("claims");
                if (claims != null && claims.get("email") != null) {
                    return claims.get("email").toString();
                }
            }
        } catch (Exception ex) {
            logger.warn("Unable to extract 'email' from event: {}", ex.getMessage());
        }
        throw new IllegalStateException("Email could not be extracted from the token claims");
    }
}
