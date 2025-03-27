package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ListingServiceRetrieveListTest {

    private ListingRepository listingRepository;
    private ListingService listingService;

    @BeforeEach
    public void setUp() {
        listingRepository = mock(ListingRepository.class);
        listingService = new ListingService(listingRepository);
    }

    @Test
    public void testGetListingSuccess() {
        String listingId = "listing-123";
        String userId = "user@example.com";

        Listing listing = new Listing();
        listing.setListingId(listingId);
        listing.setUserId(userId);
        listing.setName("Test Listing");
        listing.setCreatedAt(Instant.now());

        when(listingRepository.findById(listingId, userId)).thenReturn(listing);

        Listing result = listingService.getListing(listingId, userId);

        assertNotNull(result);
        assertEquals(listingId, result.getListingId());
        assertEquals(userId, result.getUserId());
        assertEquals("Test Listing", result.getName());
    }

    @Test
    public void testListListingsSuccess() {
        String userId = "user@example.com";

        Listing listing1 = new Listing();
        listing1.setListingId("listing-1");
        listing1.setUserId(userId);
        listing1.setName("Listing One");

        Listing listing2 = new Listing();
        listing2.setListingId("listing-2");
        listing2.setUserId(userId);
        listing2.setName("Listing Two");

        List<Listing> listings = Arrays.asList(listing1, listing2);
        when(listingRepository.findByUserId(userId)).thenReturn(listings);

        List<Listing> result = listingService.listListings(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Listing One", result.get(0).getName());
        assertEquals("Listing Two", result.get(1).getName());
    }
}
