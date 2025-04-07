package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.aop.ValidatePayload;
import it.tref.dynamicpricing.aws.lambda.dto.CreateListingRequest;
import it.tref.dynamicpricing.aws.lambda.dto.ListingSqsMessage;
import it.tref.dynamicpricing.aws.lambda.dto.UpdateListingRequest;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.repository.ListingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Provides business logic for Listing operations.
 */
@ApplicationScoped
@ValidatePayload
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final SqsProducerService sqsProducerService;

    /**
     * Constructs a new ListingService.
     *
     * @param listingRepository the repository to persist listings.
     * @param sqsProducerService the service for sending SQS messages for AI inference.
     */
    public ListingService(ListingRepository listingRepository, SqsProducerService sqsProducerService) {
        this.listingRepository = listingRepository;
        this.sqsProducerService = sqsProducerService;
    }

    /**
     * Creates a new listing from the client-supplied DTO,
     * persisting it and sending an SQS message (with the composite key
     * and attributes) for AI inference processing.
     *
     * @param request the DTO containing client-provided data.
     * @param userId  the user identifier extracted from token claims.
     * @return the generated listingId for the new listing.
     * @throws IllegalArgumentException if validation fails.
     */
    public String createListing(CreateListingRequest request, String userId) {
        Listing listing = new Listing();
        listing.setListingId(UUID.randomUUID().toString());
        listing.setUserId(userId);
        listing.setCreatedAt(Instant.now());
        listing.setName(request.getName());
        if (request.getAttributes() != null) {
            request.getAttributes().forEach(listing::addAttribute);
        }
        listing.setPrediction(new HashMap<>());
        listingRepository.save(listing);

        // Send the SQS message with the composite key and listing details for AI inference processing
        sqsProducerService.sendListingToQueue(new ListingSqsMessage(
                listing.getListingId(),
                listing.getUserId(),
                ListingSqsMessage.convertAttributes(listing.getAttributes())
        ));
        return listing.getListingId();
    }

    /**
     * Partially updates an existing listing and resets its inference state.
     * <p>
     * This method updates the listing's name and attributes if provided, then resets the
     * 'completed' flag to false and clears the prediction field. Finally, it triggers an SQS
     * message to re-run AI inference.
     * </p>
     *
     * @param listingId the identifier of the listing to update.
     * @param request   the DTO containing the update data (fields are optional).
     * @param userId    the user identifier.
     * @throws IllegalArgumentException if the listing is not found.
     */
    public void updateListing(String listingId, UpdateListingRequest request, String userId) {

        Listing existingListing = listingRepository.findById(listingId, userId);
        if (existingListing == null) {
            throw new IllegalArgumentException(String.format("Listing not found for key: (listingId) %s, (userId) %s", listingId, userId));
        }

        if (request.getName() != null) {
            existingListing.setName(request.getName());
        }
        if (request.getAttributes() != null) {
            // Replaces all the old attributes - client must send the complete list of attributes
            existingListing.setAttributes(request.getAttributes());
        }

        // Reset prediction state
        existingListing.setCompleted(false);
        existingListing.setPrediction(new HashMap<>());

        listingRepository.update(existingListing);
        logger.info("Updated listing with ID: {} for user: {}", listingId, userId);

        // Trigger the SQS message to re-run AI inference
        sqsProducerService.sendListingToQueue(new ListingSqsMessage(
                existingListing.getListingId(),
                existingListing.getUserId(),
                ListingSqsMessage.convertAttributes(existingListing.getAttributes())
        ));
    }

    /**
     * Retrieves a single listing for the specified listingId and userId.
     *
     * @param listingId the identifier of the listing.
     * @param userId    the identifier of the user.
     * @return the Listing object.
     * @throws IllegalArgumentException if no listing is found.
     */
    public Listing getListing(String listingId, String userId) {
        Listing listing = listingRepository.findById(listingId, userId);
        if (listing == null) {
            throw new IllegalArgumentException(
                    String.format("Listing not found for listingId %s and userId %s", listingId, userId)
            );
        }
        return listing;
    }

    /**
     * Retrieves all listings for the specified user.
     *
     * @param userId the unique identifier of the user.
     * @return a list of Listing objects.
     */
    public List<Listing> listListings(String userId) {
        return listingRepository.findByUserId(userId);
    }

    /**
     * Deletes a listing for the specified key (listingId and userId).
     *
     * @param listingId the identifier of the listing to be deleted.
     * @param userId    the identifier of the user.
     * @throws IllegalArgumentException if the listing is not found.
     */
    public void deleteListing(String listingId, String userId) {
        Listing existing = listingRepository.findById(listingId, userId);
        if (existing == null) {
            throw new IllegalArgumentException(
                    String.format("Listing not found for listingId %s and userId %s", listingId, userId)
            );
        }
        listingRepository.delete(listingId, userId);
    }

}
