package it.tref.dynamicpricing.aws.lambda.service;

import it.tref.dynamicpricing.aws.lambda.dto.CreateListingRequest;
import it.tref.dynamicpricing.aws.lambda.dto.CreateListingResponse;
import it.tref.dynamicpricing.aws.lambda.model.Listing;
import it.tref.dynamicpricing.aws.lambda.repository.ListingRepository;
import it.tref.dynamicpricing.aws.lambda.validation.ValidationService;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ListingServiceTest {

    private ListingRepository listingRepository;
    private ValidationService validationService;
    private ListingService listingService;

    @BeforeEach
    public void setUp() {
        listingRepository = mock(ListingRepository.class);
        validationService = mock(ValidationService.class);
        // Simulate successful validation
        when(validationService.validate(any(CreateListingRequest.class))).thenReturn(Collections.emptySet());
        listingService = new ListingService(listingRepository, validationService);
    }

    @Test
    public void testCreateListingSuccess() {
        // Prepare a valid DTO
        CreateListingRequest request = new CreateListingRequest();
        request.setName("Test Listing");

        String userId = "user@example.com";

        CreateListingResponse response = listingService.createListing(request, userId);

        assertNotNull(response);
        assertNotNull(response.getListingId());

        // Verify that repository.save() was called
        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository, times(1)).save(listingCaptor.capture());
        Listing savedListing = listingCaptor.getValue();
        assertEquals(userId, savedListing.getUserId());
        assertEquals("Test Listing", savedListing.getName());
    }

    @Test
    public void testCreateListingValidationFails() {
        CreateListingRequest request = new CreateListingRequest();
        request.setName(""); // Violates constraint

        // Simulate a validation error
        ConstraintViolation<CreateListingRequest> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Name is required");
        Set<ConstraintViolation<CreateListingRequest>> violations = Set.of(violation);
        when(validationService.validate(any(CreateListingRequest.class))).thenReturn(violations);

        String userId = "user@example.com";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            listingService.createListing(request, userId);
        });
        assertTrue(exception.getMessage().contains("Validation error"));
    }
}
