package com.example.carebloom.services.navigation;

import com.example.carebloom.dto.navigation.NavigationLocation;
import com.example.carebloom.dto.navigation.TravelTimeResult;
import com.example.carebloom.models.Mother;
import com.example.carebloom.services.midwife.RouteOptimizationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class NavigationIntegrationTest {

    @Test
    public void testCompleteNavigationFlow() {
        // Create test mothers with Colombo area coordinates
        Mother mother1 = createTestMother("1", "Mother One", 6.9271, 79.8612, "Colombo Fort");
        Mother mother2 = createTestMother("2", "Mother Two", 6.9147, 79.8728, "Pettah");
        Mother mother3 = createTestMother("3", "Mother Three", 6.9319, 79.8478, "Cinnamon Gardens");

        List<Mother> mothers = Arrays.asList(mother1, mother2, mother3);

        // Test navigation location creation
        for (Mother mother : mothers) {
            NavigationLocation navLoc = NavigationLocation.builder()
                .latitude(mother.getLatitude())
                .longitude(mother.getLongitude())
                .name(mother.getName())
                .address(mother.getAddress())
                .build();

            assertTrue(navLoc.hasValidCoordinates(), 
                "Mother " + mother.getName() + " should have valid coordinates");
            assertNotNull(navLoc.getLatLngString());
            assertTrue(navLoc.getLatLngString().contains(","));
        }

        // Test travel time calculation between locations
        GoogleMapsDistanceService distanceService = new GoogleMapsDistanceService();
        
        NavigationLocation from = NavigationLocation.builder()
            .latitude(mother1.getLatitude())
            .longitude(mother1.getLongitude())
            .name(mother1.getName())
            .build();

        NavigationLocation to = NavigationLocation.builder()
            .latitude(mother2.getLatitude())
            .longitude(mother2.getLongitude())
            .name(mother2.getName())
            .build();

        TravelTimeResult result = distanceService.getTravelTime(from, to);

        assertNotNull(result, "Travel time result should not be null");
        assertTrue(result.getDurationSeconds() >= 0, "Duration should be non-negative");
        assertTrue(result.getDistanceMeters() > 0, "Distance should be positive");
        assertNotNull(result.getStatus(), "Status should not be null");

        // Test route optimization with navigation data
        RouteOptimizationService routeService = new RouteOptimizationService();
        
        // Verify all mothers have valid coordinates
        for (Mother mother : mothers) {
            assertTrue(routeService.hasValidCoordinates(mother), 
                "Mother " + mother.getName() + " should have valid coordinates for optimization");
        }

        // Test route optimization
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        List<Mother> optimizedOrder = routeService.optimizeVisitOrder(mothers, startTime, endTime);

        assertNotNull(optimizedOrder, "Optimized order should not be null");
        assertEquals(mothers.size(), optimizedOrder.size(), "Optimized order should contain all mothers");
        
        // Verify all original mothers are present in optimized order
        for (Mother originalMother : mothers) {
            assertTrue(optimizedOrder.stream()
                .anyMatch(optimized -> optimized.getId().equals(originalMother.getId())),
                "Original mother " + originalMother.getName() + " should be present in optimized order");
        }

        System.out.println("✓ Navigation Integration Test Completed Successfully");
        System.out.println("  - Original order: " + getMotherNames(mothers));
        System.out.println("  - Optimized order: " + getMotherNames(optimizedOrder));
        System.out.println("  - Travel time example: " + result.getDurationSeconds() + " seconds");
        System.out.println("  - Distance example: " + Math.round(result.getDistanceMeters()) + " meters");
        System.out.println("  - Status: " + result.getStatus());
    }

    @Test
    public void testBatchTravelTimeCalculation() {
        GoogleMapsDistanceService distanceService = new GoogleMapsDistanceService();

        // Create test locations in Colombo area
        List<NavigationLocation> locations = Arrays.asList(
            NavigationLocation.builder().latitude(6.9271).longitude(79.8612).name("Colombo").build(),
            NavigationLocation.builder().latitude(6.9147).longitude(79.8728).name("Pettah").build(),
            NavigationLocation.builder().latitude(6.9319).longitude(79.8478).name("Cinnamon Gardens").build()
        );

        TravelTimeResult[][] matrix = distanceService.getTravelTimeMatrix(locations);

        assertNotNull(matrix, "Travel time matrix should not be null");
        assertEquals(locations.size(), matrix.length, "Matrix should have correct number of rows");
        assertEquals(locations.size(), matrix[0].length, "Matrix should have correct number of columns");

        // Check diagonal (same location to same location)
        for (int i = 0; i < locations.size(); i++) {
            assertEquals(0, matrix[i][i].getDurationSeconds(), 
                "Same location travel time should be zero");
        }

        // Check non-diagonal elements
        for (int i = 0; i < locations.size(); i++) {
            for (int j = 0; j < locations.size(); j++) {
                if (i != j) {
                    assertNotNull(matrix[i][j], "Travel time result should not be null");
                    assertTrue(matrix[i][j].getDurationSeconds() >= 0, 
                        "Travel time should be non-negative");
                }
            }
        }

        System.out.println("✓ Batch Travel Time Calculation Test Completed Successfully");
        System.out.println("  - Matrix size: " + locations.size() + "x" + locations.size());
        System.out.println("  - Cache stats: " + distanceService.getCacheStats());
    }

    private Mother createTestMother(String id, String name, double lat, double lng, String address) {
        Mother mother = new Mother();
        mother.setId(id);
        mother.setName(name);
        mother.setLatitude(lat);
        mother.setLongitude(lng);
        mother.setAddress(address);
        mother.setLocationAddress(address);
        return mother;
    }

    private String getMotherNames(List<Mother> mothers) {
        return mothers.stream()
            .map(Mother::getName)
            .reduce((a, b) -> a + " → " + b)
            .orElse("Empty");
    }
}
