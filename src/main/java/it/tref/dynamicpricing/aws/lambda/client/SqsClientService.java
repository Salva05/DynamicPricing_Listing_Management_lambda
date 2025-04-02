package it.tref.dynamicpricing.aws.lambda.client;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import it.tref.dynamicpricing.aws.lambda.config.ConfigService;

/**
 * Service for creating and providing an SQS client.
 * <p>
 * This class builds an instance of {@link SqsClient} using configuration from the {@link ConfigService}.
 * </p>
 */
@ApplicationScoped
public class SqsClientService {

    private final SqsClient sqsClient;

    /**
     * Constructs a new SqsClientService.
     * <p>
     * The client is built using the AWS region provided by the {@link ConfigService}.
     * </p>
     *
     * @param configService the configuration service that provides SQS settings.
     */
    public SqsClientService(ConfigService configService) {
        this.sqsClient = SqsClient.builder()
                .region(Region.of(configService.getSqsQueueRegion()))
                .build();
    }

    /**
     * Returns the {@link SqsClient} instance.
     *
     * @return the SqsClient instance.
     */
    public SqsClient getSqsClient() {
        return sqsClient;
    }

    /**
     * Closes the SqsClient when the bean is destroyed.
     */
    @PreDestroy
    public void close() {
        if (sqsClient != null) {
            sqsClient.close();
        }
    }
}
