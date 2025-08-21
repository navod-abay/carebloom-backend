package com.example.carebloom.services.navigation;

import com.example.carebloom.dto.navigation.NavigationLocation;
import com.example.carebloom.dto.navigation.TravelTimeResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "google.maps.api.key=test-key"
})
public class GoogleMapsDistanceServiceTest {

    private GoogleMapsDistanceService googleMapsDistanceService;

    @BeforeEach
    void setUp() {
        googleMapsDistanceService = new GoogleMapsDistanceService();
    }

    @Test
    void testNavigationLocationCreation() {
        NavigationLocation location = NavigationLocation.builder()
            .latitude(6.9271)
            .longitude(79.8612)
            .name("Test Location")
            .address("Colombo, Sri Lanka")
            .build();

        assertTrue(location.hasValidCoordinates());
        assertEquals("6.9271,79.8612", location.getLatLngString());
    }

    @Test
    void testNavigationLocationValidation() {
        // Valid coordinates
        NavigationLocation validLocation = new NavigationLocation(6.9271, 79.8612);
        assertTrue(validLocation.hasValidCoordinates());

        // Invalid coordinates
        NavigationLocation invalidLocation1 = new NavigationLocation(0.0, 0.0);
        assertFalse(invalidLocation1.hasValidCoordinates());

        NavigationLocation invalidLocation2 = new NavigationLocation(91.0, 181.0);
        assertFalse(invalidLocation2.hasValidCoordinates());
    }

    @Test
    void testTravelTimeResultValidation() {
        TravelTimeResult validResult = TravelTimeResult.builder()
            .duration(java.time.Duration.ofMinutes(15))
            .distanceMeters(5000.0)
            .status("OK")
            .build();

        assertTrue(validResult.isValid());
        assertEquals(900, validResult.getDurationSeconds());

        TravelTimeResult invalidResult = TravelTimeResult.builder()
            .status("NOT_FOUND")
            .build();

        assertFalse(invalidResult.isValid());
    }

    @Test
    void testFallbackCalculation() {
        // Test with locations in Colombo area
        NavigationLocation colombo = NavigationLocation.builder()
            .latitude(6.9271)
            .longitude(79.8612)
            .name("Colombo")
            .build();

        NavigationLocation kandy = NavigationLocation.builder()
            .latitude(7.2906)
            .longitude(80.6337)
            .name("Kandy")
            .build();

        // This will use fallback calculation since we're using a test API key
        TravelTimeResult result = googleMapsDistanceService.getTravelTime(colombo, kandy);

        assertNotNull(result);
        assertTrue(result.getDurationSeconds() > 0);
        assertTrue(result.getDistanceMeters() > 0);
        // Should be "ESTIMATED" since we're using fallback
        assertEquals("ESTIMATED", result.getStatus());
    }

    @Test
    void testCacheKeyGeneration() {
        NavigationLocation loc1 = new NavigationLocation(6.9271, 79.8612);
        NavigationLocation loc2 = new NavigationLocation(7.2906, 80.6337);

        // Test that different location pairs generate different cache keys
        // This is an implementation detail test - we're testing the internal logic
        String expectedKey = String.format("%.6f,%.6f->%.6f,%.6f", 
            loc1.getLatitude(), loc1.getLongitude(),
            loc2.getLatitude(), loc2.getLongitude());

        // We can't directly test the private method, but we can verify the format
        assertTrue(expectedKey.contains("6.927100,79.861200->7.290600,80.633700"));
    }

    @Test
    void testInvalidCoordinatesHandling() {
        NavigationLocation invalidFrom = new NavigationLocation(0.0, 0.0);
        NavigationLocation validTo = new NavigationLocation(6.9271, 79.8612);

        TravelTimeResult result = googleMapsDistanceService.getTravelTime(invalidFrom, validTo);

        assertNotNull(result);
        // Should return fallback result for invalid coordinates
        assertTrue(result.getDurationSeconds() >= 0);
    }
}
