package it.tref.dynamicpricing.aws.lambda.repository;

import it.tref.dynamicpricing.aws.lambda.model.Listing;

import java.util.List;

/**
 * Interface for managing Listing objects at persistence layer.
 */
public interface ListingRepository {
    /**
     * Persists a listing in the datastore.
     *
     * @param listing the listing to persist.
     */
    void save(Listing listing);

    /**
     * Updates an existing listing in the datastore.
     *
     * @param listing the listing with updated data.
     */
    void update(Listing listing);

    /**
     * Finds a listing by its composite primary key (listingId and userId).
     *
     * @param listingId the unique identifier for the listing.
     * @param userId    the unique identifier for the user.
     * @return the Listing if found, or null if not found.
     */
    Listing findById(String listingId, String userId);

    /**
     * Retrieves all listings associated with the specified user.
     *
     * @param userId the unique identifier of the user.
     * @return a list of Listing objects; may be empty if none are found.
     */
    List<Listing> findByUserId(String userId);

    /**
     * Deletes a listing for the given listingId and userId.
     *
     * @param listingId the identifier of the listing.
     * @param userId    the identifier of the user.
     */
    void delete(String listingId, String userId);
}
