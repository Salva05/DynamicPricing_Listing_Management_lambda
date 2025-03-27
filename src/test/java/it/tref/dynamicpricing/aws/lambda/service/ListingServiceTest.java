package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.dto.CreateListingRequest;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ListingServiceTest {

    private ListingRepository listingRepository;
    private ListingService listingService;

    @BeforeEach
    public void setUp() {
        listingRepository = mock(ListingRepository.class);
        listingService = new ListingService(listingRepository);
    }

    @Test
    public void testCreateListingSuccess() {
        // Prepare a valid CreateListingRequest DTO
        CreateListingRequest request = new CreateListingRequest();
        request.setName("Test Listing");

        String userId = "user@example.com";

        String newListingId = listingService.createListing(request, userId);

        assertNotNull(newListingId);

        // Capture the Listing object passed to repository.save()
        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository, times(1)).save(listingCaptor.capture());
        Listing savedListing = listingCaptor.getValue();

        // Verify the properties of the saved listing
        assertEquals(userId, savedListing.getUserId());
        assertEquals("Test Listing", savedListing.getName());
    }
}
