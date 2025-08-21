package com.example.carebloom.services.midwife;

import com.example.carebloom.config.CustomAuthenticationToken;
import com.example.carebloom.dto.midwife.CalculateVisitOrderDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderResponseDTO;
import com.example.carebloom.models.FieldVisit;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.FieldVisitRepository;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MotherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RouteOptimizationIntegrationTest {

    @Autowired
    private FieldVisitService fieldVisitService;

    @Autowired
    private RouteOptimizationService routeOptimizationService;

    @Autowired
    private FieldVisitRepository fieldVisitRepository;

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private MotherRepository motherRepository;

    private Midwife testMidwife;
    private FieldVisit testFieldVisit;
    private List<Mother> testMothers;

    @BeforeEach
    void setUp() {
        // Clean up data
        fieldVisitRepository.deleteAll();
        motherRepository.deleteAll();
        midwifeRepository.deleteAll();

        // Create test data
        testMidwife = createAndSaveTestMidwife();
        testMothers = createAndSaveTestMothers();
        testFieldVisit = createAndSaveTestFieldVisit();

        // Set up security context with custom token
        CustomAuthenticationToken authToken = 
            new CustomAuthenticationToken(
                testMidwife.getFirebaseUid(), 
                null, // credentials
                new ArrayList<>(), // authorities
                testMidwife.getId(),
                "MIDWIFE", 
                testMidwife
            );
        authToken.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Test
    void testCompleteRouteOptimizationFlow_WithCoordinates_Success() {
        // Given
        CalculateVisitOrderDTO request = new CalculateVisitOrderDTO();
        request.setOverrideUnconfirmed(true);

        // When
        CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder(
            testFieldVisit.getId(), request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getVisitOrder());
        assertEquals(testMothers.size(), result.getVisitOrder().size());
        
        // Verify all mothers are included
        List<String> resultMotherIds = result.getVisitOrder().stream()
            .map(CalculateVisitOrderResponseDTO.VisitOrder::getMotherId)
            .toList();
        
        for (Mother mother : testMothers) {
            assertTrue(resultMotherIds.contains(mother.getId()));
        }

        // Verify response has correct structure
        for (CalculateVisitOrderResponseDTO.VisitOrder order : result.getVisitOrder()) {
            assertNotNull(order.getMotherId());
            assertNotNull(order.getMotherName());
            assertNotNull(order.getAddress());
            assertNotNull(order.getEstimatedArrivalTime());
            assertNotNull(order.getEstimatedDuration());
            assertNotNull(order.getDistance());
            assertTrue(order.getEstimatedDuration() > 0);
            assertTrue(order.getDistance() >= 0);
        }

        // Verify totals are calculated
        assertTrue(result.getTotalDistance() >= 0);
        assertTrue(result.getTotalEstimatedTime() > 0);
    }

    @Test
    void testRouteOptimizationService_DirectCall_WithValidCoordinates() {
        // Given
        java.time.LocalTime startTime = java.time.LocalTime.of(9, 0);
        java.time.LocalTime endTime = java.time.LocalTime.of(17, 0);

        // When
        List<Mother> optimizedOrder = routeOptimizationService.optimizeVisitOrder(
            testMothers, startTime, endTime);

        // Then
        assertNotNull(optimizedOrder);
        assertEquals(testMothers.size(), optimizedOrder.size());
        
        // Verify all mothers are included
        List<String> inputIds = testMothers.stream().map(Mother::getId).toList();
        List<String> outputIds = optimizedOrder.stream().map(Mother::getId).toList();
        assertTrue(outputIds.containsAll(inputIds));
    }

    @Test
    void testCoordinateValidation_RouteOptimizationService() {
        // Test valid coordinates
        for (Mother mother : testMothers) {
            assertTrue(routeOptimizationService.hasValidCoordinates(mother),
                "Mother " + mother.getName() + " should have valid coordinates");
        }

        // Test invalid coordinates
        Mother invalidMother = new Mother();
        invalidMother.setLatitude(91.0); // Invalid latitude
        invalidMother.setLongitude(79.8612);
        assertFalse(routeOptimizationService.hasValidCoordinates(invalidMother));

        invalidMother.setLatitude(6.9271);
        invalidMother.setLongitude(181.0); // Invalid longitude
        assertFalse(routeOptimizationService.hasValidCoordinates(invalidMother));

        invalidMother.setLatitude(null);
        invalidMother.setLongitude(null);
        assertFalse(routeOptimizationService.hasValidCoordinates(invalidMother));
    }

    @Test
    void testRouteOptimization_PerformanceWithLargeDataset() {
        // Given - Create a larger dataset
        List<Mother> largeMothersSet = createLargeMothersDataset(20);
        motherRepository.saveAll(largeMothersSet);

        // Update field visit to include all mothers
        testFieldVisit.setSelectedMotherIds(
            largeMothersSet.stream().map(Mother::getId).toList());
        fieldVisitRepository.save(testFieldVisit);

        CalculateVisitOrderDTO request = new CalculateVisitOrderDTO();
        request.setOverrideUnconfirmed(true);

        // When
        long startTime = System.currentTimeMillis();
        CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder(
            testFieldVisit.getId(), request);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(largeMothersSet.size(), result.getVisitOrder().size());
        
        // Should complete within reasonable time
        long duration = endTime - startTime;
        assertTrue(duration < 35000, "Route optimization took too long: " + duration + "ms");
    }

    @Test
    void testFallbackBehavior_WhenOrToolsFails() {
        // Given - Create mothers without coordinates to force fallback
        List<Mother> mothersWithoutCoords = createMothersWithoutCoordinates();
        motherRepository.saveAll(mothersWithoutCoords);

        testFieldVisit.setSelectedMotherIds(
            mothersWithoutCoords.stream().map(Mother::getId).toList());
        fieldVisitRepository.save(testFieldVisit);

        CalculateVisitOrderDTO request = new CalculateVisitOrderDTO();
        request.setOverrideUnconfirmed(true);

        // When
        CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder(
            testFieldVisit.getId(), request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mothersWithoutCoords.size(), result.getVisitOrder().size());
        
        // Should still provide a valid ordering
        List<String> times = result.getVisitOrder().stream()
            .map(CalculateVisitOrderResponseDTO.VisitOrder::getEstimatedArrivalTime)
            .toList();
        
        // Times should be in ascending order
        for (int i = 1; i < times.size(); i++) {
            assertTrue(times.get(i - 1).compareTo(times.get(i)) <= 0);
        }
    }

    // Helper methods for creating test data

    private Midwife createAndSaveTestMidwife() {
        Midwife midwife = new Midwife();
        midwife.setFirebaseUid("test-midwife-uid");
        midwife.setName("Test Midwife");
        midwife.setEmail("test.midwife@example.com");
        midwife.setPhone("0771234567");
        return midwifeRepository.save(midwife);
    }

    private List<Mother> createAndSaveTestMothers() {
        List<Mother> mothers = List.of(
            createMotherWithCoordinates("Mother 1", 6.9271, 79.8612, "10:00"), // Colombo
            createMotherWithCoordinates("Mother 2", 6.9350, 79.8500, "09:30"), // Colombo 7
            createMotherWithCoordinates("Mother 3", 6.9147, 79.8728, "11:00"), // Colombo 3
            createMotherWithCoordinates("Mother 4", 6.9204, 79.8740, "09:45"), // Fort
            createMotherWithCoordinates("Mother 5", 6.9244, 79.8553, "10:30")  // Pettah
        );
        return motherRepository.saveAll(mothers);
    }

    private Mother createMotherWithCoordinates(String name, Double latitude, Double longitude, String appointmentTime) {
        Mother mother = new Mother();
        mother.setFirebaseUid("uid-" + name.replace(" ", "").toLowerCase());
        mother.setName(name);
        mother.setEmail(name.replace(" ", "").toLowerCase() + "@example.com");
        mother.setPhone("077123456" + name.charAt(name.length() - 1));
        mother.setAddress("Address for " + name);
        mother.setLatitude(latitude);
        mother.setLongitude(longitude);
        mother.setLocationAddress("GPS Location for " + name);
        mother.setRegistrationStatus("completed");
        mother.setAreaMidwifeId(testMidwife.getId());

        // Add field visit appointment
        Mother.FieldVisitAppointment appointment = new Mother.FieldVisitAppointment();
        appointment.setDate("2024-12-20");
        appointment.setStartTime(appointmentTime);
        appointment.setEndTime("17:00");
        appointment.setStatus("confirmed");
        mother.setFieldVisitAppointment(appointment);

        return mother;
    }

    private FieldVisit createAndSaveTestFieldVisit() {
        FieldVisit fieldVisit = new FieldVisit();
        fieldVisit.setMidwifeId(testMidwife.getId());
        fieldVisit.setDate("2024-12-20");
        fieldVisit.setStartTime("09:00");
        fieldVisit.setEndTime("17:00");
        fieldVisit.setSelectedMotherIds(testMothers.stream().map(Mother::getId).toList());
        fieldVisit.setStatus("SCHEDULED");
        fieldVisit.setCreatedAt(LocalDateTime.now());
        fieldVisit.setUpdatedAt(LocalDateTime.now());

        FieldVisit savedFieldVisit = fieldVisitRepository.save(fieldVisit);

        // Update mothers with visit ID
        for (Mother mother : testMothers) {
            mother.getFieldVisitAppointment().setVisitId(savedFieldVisit.getId());
            motherRepository.save(mother);
        }

        return savedFieldVisit;
    }

    private List<Mother> createLargeMothersDataset(int count) {
        List<Mother> mothers = new java.util.ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // Generate coordinates around Colombo area
            double lat = 6.9271 + (Math.random() - 0.5) * 0.1; // ±~5km variation
            double lon = 79.8612 + (Math.random() - 0.5) * 0.1;
            String appointmentTime = String.format("%02d:%02d", 
                9 + (i % 8), // 9 AM to 4 PM
                (i * 15) % 60); // 15-minute intervals
            
            mothers.add(createMotherWithCoordinates("Mother " + (i + 1), lat, lon, appointmentTime));
        }
        
        return mothers;
    }

    private List<Mother> createMothersWithoutCoordinates() {
        return List.of(
            createMotherWithoutCoordinates("Mother A", "09:30"),
            createMotherWithoutCoordinates("Mother B", "10:00"),
            createMotherWithoutCoordinates("Mother C", "09:15"),
            createMotherWithoutCoordinates("Mother D", "11:00")
        );
    }

    private Mother createMotherWithoutCoordinates(String name, String appointmentTime) {
        Mother mother = new Mother();
        mother.setFirebaseUid("uid-" + name.replace(" ", "").toLowerCase());
        mother.setName(name);
        mother.setEmail(name.replace(" ", "").toLowerCase() + "@example.com");
        mother.setPhone("077123456" + name.charAt(name.length() - 1));
        mother.setAddress("Address for " + name);
        // No coordinates set
        mother.setRegistrationStatus("completed");
        mother.setAreaMidwifeId(testMidwife.getId());

        Mother.FieldVisitAppointment appointment = new Mother.FieldVisitAppointment();
        appointment.setDate("2024-12-20");
        appointment.setStartTime(appointmentTime);
        appointment.setEndTime("17:00");
        appointment.setStatus("confirmed");
        mother.setFieldVisitAppointment(appointment);

        return mother;
    }
}
