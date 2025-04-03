package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.client.SqsClientService;
import it.tref.dynamicpricing.aws.lambda.config.ConfigService;
import it.tref.dynamicpricing.aws.lambda.dto.ListingSqsMessage;
import it.tref.dynamicpricing.aws.lambda.mapper.MapperService;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Service for sending messages to the SQS queue to trigger AI inference.
 * <p>
 * This service serializes a {@link ListingSqsMessage} to JSON using the {@link MapperService}
 * and sends it to the configured SQS queue.
 * The message includes the composite key (listingId and userId) and the listing details required for processing.
 * </p>
 */
@ApplicationScoped
public class SqsProducerService {

    private static final Logger logger = LoggerFactory.getLogger(SqsProducerService.class);

    private final SqsClientService sqsClientService;
    private final ConfigService configService;
    private final MapperService mapperService;

    /**
     * Constructs a new SqsProducerService.
     * <p>
     * The service is built using the SqsClientService for accessing the SQS client,
     * the ConfigService for retrieving configuration values, and the MapperService for JSON serialization.
     * </p>
     *
     * @param sqsClientService the service providing the SQS client.
     * @param configService    the configuration service that provides the SQS queue URL.
     * @param mapperService    the service for JSON serialization and deserialization.
     */
    public SqsProducerService(SqsClientService sqsClientService, ConfigService configService, MapperService mapperService) {
        this.sqsClientService = sqsClientService;
        this.configService = configService;
        this.mapperService = mapperService;
    }

    /**
     * Sends a listing message to the SQS queue.
     * <p>
     * This method converts a {@link ListingSqsMessage} into a JSON payload using the {@link MapperService}
     * and sends it using the SQS client.
     * </p>
     *
     * @param message the {@link ListingSqsMessage} containing the listing's composite key and details.
     */
    public void sendListingToQueue(ListingSqsMessage message) {
        String queueUrl = configService.getSqsQueueUrl();
        String messageBody = mapperService.writeValueAsString(message);

        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();

        SqsClient client = sqsClientService.getSqsClient();
        client.sendMessage(request);
        logger.info("Successfully sent SQS message for listingId: {}", message.getListingId());
    }
}
