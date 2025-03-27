package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

public class ListingServiceDeleteTest {

    private ListingRepository listingRepository;
    private ListingService listingService;

    @BeforeEach
    public void setUp() {
        listingRepository = mock(ListingRepository.class);
        listingService = new ListingService(listingRepository);
    }

    @Test
    public void testDeleteListingSuccess() {
        String listingId = "testId";
        String userId = "user@example.com";
        Listing listing = new Listing();
        listing.setListingId(listingId);
        listing.setUserId(userId);
        listing.setName("Test Listing");

        // Simulate that the listing exists.
        when(listingRepository.findById(listingId, userId)).thenReturn(listing);

        // Call deleteListing.
        listingService.deleteListing(listingId, userId);

        // Verify that repository.delete() was invoked.
        verify(listingRepository, times(1)).delete(listingId, userId);
    }

    @Test
    public void testDeleteListingNotFound() {
        String listingId = "nonexistent";
        String userId = "user@example.com";

        when(listingRepository.findById(listingId, userId)).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                listingService.deleteListing(listingId, userId));
        assertTrue(exception.getMessage().contains("Listing not found"));
    }
}

