package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.dto.CreateListingRequest;
import it.tref.dynamicpricing.aws.lambda.dto.CreateListingResponse;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.repository.ListingRepository;
import it.tref.dynamicpricing.aws.lambda.validation.ValidationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provides business logic for Listing operations.
 */
@ApplicationScoped
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final ValidationService validationService;

    /**
     * Constructs a new ListingService.
     *
     * @param listingRepository the repository to persist listings.
     * @param validationService the service to validate incoming data.
     */
    public ListingService(ListingRepository listingRepository, ValidationService validationService) {
        this.listingRepository = listingRepository;
        this.validationService = validationService;
    }

    /**
     * Creates a new listing from the client-supplied DTO.
     *
     * @param request the DTO containing client-provided data.
     * @param userId  the user identifier extracted from token claims.
     * @return a response DTO containing the generated listingId.
     * @throws IllegalArgumentException if validation fails.
     */
    public CreateListingResponse createListing(CreateListingRequest request, String userId) {
        // Validate the incoming DTO
        Set<ConstraintViolation<CreateListingRequest>> violations = validationService.validate(request);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            logger.warn("Validation failed: {}", errorMessage);
            throw new IllegalArgumentException("Validation error: " + errorMessage);
        }

        // Map the DTO to the internal Listing model
        Listing listing = new Listing();
        listing.setListingId(UUID.randomUUID().toString());
        listing.setUserId(userId);
        listing.setCreatedAt(Instant.now());
        listing.setName(request.getName());
        if (request.getAttributes() != null) {
            request.getAttributes().forEach(listing::addAttribute);
        }

        // Persist the listing
        listingRepository.save(listing);

        // Build and return the response DTO
        CreateListingResponse response = new CreateListingResponse();
        response.setListingId(listing.getListingId());
        logger.info("Created listing with ID: {} for user: {}", listing.getListingId(), userId);
        return response;
    }
}
