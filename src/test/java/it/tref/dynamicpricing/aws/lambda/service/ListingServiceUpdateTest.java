package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.dto.UpdateListingRequest;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ListingServiceUpdateTest {

    private ListingRepository listingRepository;
    private ListingService listingService;

    @BeforeEach
    public void setUp() {
        listingRepository = mock(ListingRepository.class);
        listingService = new ListingService(listingRepository);
    }

    @Test
    public void testUpdateListingSuccess() {
        // Prepare existing listing
        String listingId = "testId";
        String userId = "user@example.com";
        Listing existingListing = new Listing();
        existingListing.setListingId(listingId);
        existingListing.setUserId(userId);
        existingListing.setName("Old Listing");
        // add attribute
        existingListing.addAttribute("color", "red");

        // Simulate repository returning the existing listing
        when(listingRepository.findById(listingId, userId)).thenReturn(existingListing);

        // Prepare an update request
        UpdateListingRequest updateRequest = new UpdateListingRequest();
        updateRequest.setName("New Listing");

        // Call the service method
        listingService.updateListing(listingId, updateRequest, userId);

        // Verify that the listing was updated
        assertEquals("New Listing", existingListing.getName());
        // Attributes remain unchanged if not updated
        assertEquals("red", existingListing.getAttributes().get("color"));

        // Verify that repository.update() was called
        verify(listingRepository, times(1)).update(existingListing);
    }

    @Test
    public void testUpdateListingNotFound() {
        String listingId = "nonexistent";
        String userId = "user@example.com";
        // Simulate that the listing is not found
        when(listingRepository.findById(listingId, userId)).thenReturn(null);

        UpdateListingRequest updateRequest = new UpdateListingRequest();
        updateRequest.setName("New Name");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                listingService.updateListing(listingId, updateRequest, userId));
        assertTrue(exception.getMessage().contains("Listing not found"));
    }

    @Test
    public void testUpdateListingDynamoDbError() {
        // Prepare an existing listing
        String listingId = "testId";
        String userId = "user@example.com";
        Listing existingListing = new Listing();
        existingListing.setListingId(listingId);
        existingListing.setUserId(userId);
        existingListing.setName("Old Listing");

        when(listingRepository.findById(listingId, userId)).thenReturn(existingListing);

        // Prepare an update request
        UpdateListingRequest updateRequest = new UpdateListingRequest();
        updateRequest.setName("New Listing");

        // Simulate repository.update() throwing an exception
        RuntimeException dynamoException = new RuntimeException("DynamoDB error");
        doThrow(dynamoException).when(listingRepository).update(existingListing);

        Exception thrown = assertThrows(RuntimeException.class, () ->
                listingService.updateListing(listingId, updateRequest, userId));
        assertEquals("DynamoDB error", thrown.getMessage());
    }
}
