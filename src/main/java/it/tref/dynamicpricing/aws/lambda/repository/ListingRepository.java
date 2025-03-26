package it.tref.dynamicpricing.aws.lambda.repository;

import it.tref.dynamicpricing.aws.lambda.model.Listing;

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
}
