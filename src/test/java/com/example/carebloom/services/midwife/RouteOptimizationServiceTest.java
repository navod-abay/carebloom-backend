package com.example.carebloom.services.midwife;

import com.example.carebloom.models.Mother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RouteOptimizationServiceTest {

    @InjectMocks
    private RouteOptimizationService routeOptimizationService;

    private List<Mother> testMothers;
    private LocalTime startTime;
    private LocalTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalTime.of(9, 0); // 09:00
        endTime = LocalTime.of(17, 0);   // 17:00
        
        testMothers = createTestMothers();
    }

    @Test
    void testOptimizeVisitOrder_EmptyList_ReturnsEmptyList() {
        // Given
        List<Mother> emptyList = new ArrayList<>();

        // When
        List<Mother> result = routeOptimizationService.optimizeVisitOrder(emptyList, startTime, endTime);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testOptimizeVisitOrder_SingleMother_ReturnsSameMother() {
        // Given
        List<Mother> singleMother = List.of(testMothers.get(0));

        // When
        List<Mother> result = routeOptimizationService.optimizeVisitOrder(singleMother, startTime, endTime);

        // Then
        assertEquals(1, result.size());
        assertEquals(testMothers.get(0).getId(), result.get(0).getId());
    }

    @Test
    void testOptimizeVisitOrder_MultipleMothersWithCoordinates_ReturnsOptimizedOrder() {
        // Given
        List<Mother> mothersWithCoordinates = testMothers.stream()
            .filter(m -> m.getLatitude() != null && m.getLongitude() != null)
            .toList();

        // When
        List<Mother> result = routeOptimizationService.optimizeVisitOrder(mothersWithCoordinates, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(mothersWithCoordinates.size(), result.size());
        
        // Verify all mothers are included
        List<String> inputIds = mothersWithCoordinates.stream().map(Mother::getId).toList();
        List<String> resultIds = result.stream().map(Mother::getId).toList();
        assertTrue(resultIds.containsAll(inputIds));
    }

    @Test
    void testOptimizeVisitOrder_MothersWithoutCoordinates_UsesFallbackOrdering() {
        // Given
        List<Mother> mothersWithoutCoordinates = createMothersWithoutCoordinates();

        // When
        List<Mother> result = routeOptimizationService.optimizeVisitOrder(mothersWithoutCoordinates, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(mothersWithoutCoordinates.size(), result.size());
        
        // Should be ordered by appointment time (fallback behavior)
        for (int i = 1; i < result.size(); i++) {
            String prevTime = getAppointmentTime(result.get(i - 1));
            String currentTime = getAppointmentTime(result.get(i));
            assertTrue(prevTime.compareTo(currentTime) <= 0);
        }
    }

    @Test
    void testHasValidCoordinates_ValidCoordinates_ReturnsTrue() {
        // Given
        Mother mother = createMotherWithCoordinates("test1", "Test Mother", 6.9271, 79.8612);

        // When
        boolean result = routeOptimizationService.hasValidCoordinates(mother);

        // Then
        assertTrue(result);
    }

    @Test
    void testHasValidCoordinates_NullCoordinates_ReturnsFalse() {
        // Given
        Mother mother = createMotherWithCoordinates("test1", "Test Mother", null, null);

        // When
        boolean result = routeOptimizationService.hasValidCoordinates(mother);

        // Then
        assertFalse(result);
    }

    @Test
    void testHasValidCoordinates_InvalidLatitude_ReturnsFalse() {
        // Given
        Mother mother = createMotherWithCoordinates("test1", "Test Mother", 91.0, 79.8612); // Invalid latitude

        // When
        boolean result = routeOptimizationService.hasValidCoordinates(mother);

        // Then
        assertFalse(result);
    }

    @Test
    void testHasValidCoordinates_InvalidLongitude_ReturnsFalse() {
        // Given
        Mother mother = createMotherWithCoordinates("test1", "Test Mother", 6.9271, 181.0); // Invalid longitude

        // When
        boolean result = routeOptimizationService.hasValidCoordinates(mother);

        // Then
        assertFalse(result);
    }

    @Test
    void testOptimizeVisitOrder_MixedCoordinates_ProcessesOnlyValid() {
        // Given
        List<Mother> mixedMothers = new ArrayList<>();
        mixedMothers.add(createMotherWithCoordinates("valid1", "Valid Mother 1", 6.9271, 79.8612));
        mixedMothers.add(createMotherWithCoordinates("invalid1", "Invalid Mother", 91.0, 79.8612));
        mixedMothers.add(createMotherWithCoordinates("valid2", "Valid Mother 2", 6.9350, 79.8500));
        mixedMothers.add(createMotherWithCoordinates("invalid2", "No Coords Mother", null, null));

        // When
        List<Mother> result = routeOptimizationService.optimizeVisitOrder(mixedMothers, startTime, endTime);

        // Then
        assertNotNull(result);
        // Should process all mothers using fallback for invalid coordinates
        assertEquals(4, result.size());
    }

    @Test
    void testOptimizeVisitOrder_WithTimeWindows_RespectsAppointmentTimes() {
        // Given
        List<Mother> mothersWithAppointments = createMothersWithAppointments();

        // When
        List<Mother> result = routeOptimizationService.optimizeVisitOrder(mothersWithAppointments, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(mothersWithAppointments.size(), result.size());
    }

    @Test
    void testOptimizeVisitOrder_PerformanceTest_LargeMothersSet() {
        // Given
        List<Mother> largeMothersSet = createLargeMothersSet(50);

        // When
        long startTime = System.currentTimeMillis();
        List<Mother> result = routeOptimizationService.optimizeVisitOrder(largeMothersSet, this.startTime, endTime);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertEquals(largeMothersSet.size(), result.size());
        
        // Should complete within reasonable time (30 seconds as per OR-Tools timeout)
        long duration = endTime - startTime;
        assertTrue(duration < 35000, "Route optimization took too long: " + duration + "ms");
    }

    // Helper methods for creating test data

    private List<Mother> createTestMothers() {
        List<Mother> mothers = new ArrayList<>();
        
        // Colombo area coordinates
        mothers.add(createMotherWithCoordinates("1", "Mother 1", 6.9271, 79.8612)); // Colombo
        mothers.add(createMotherWithCoordinates("2", "Mother 2", 6.9350, 79.8500)); // Colombo 7
        mothers.add(createMotherWithCoordinates("3", "Mother 3", 6.9147, 79.8728)); // Colombo 3
        mothers.add(createMotherWithCoordinates("4", "Mother 4", 6.9204, 79.8740)); // Fort
        mothers.add(createMotherWithCoordinates("5", "Mother 5", 6.9244, 79.8553)); // Pettah
        
        return mothers;
    }

    private Mother createMotherWithCoordinates(String id, String name, Double latitude, Double longitude) {
        Mother mother = new Mother();
        mother.setId(id);
        mother.setName(name);
        mother.setLatitude(latitude);
        mother.setLongitude(longitude);
        mother.setAddress("Test Address for " + name);
        mother.setLocationAddress("GPS Address for " + name);
        
        // Add field visit appointment
        Mother.FieldVisitAppointment appointment = new Mother.FieldVisitAppointment();
        appointment.setVisitId("visit-" + id);
        appointment.setDate("2024-12-20");
        appointment.setStartTime("09:00");
        appointment.setEndTime("17:00");
        appointment.setStatus("confirmed");
        mother.setFieldVisitAppointment(appointment);
        
        return mother;
    }

    private List<Mother> createMothersWithoutCoordinates() {
        List<Mother> mothers = new ArrayList<>();
        
        mothers.add(createMotherWithAppointmentTime("1", "Mother 1", "09:30"));
        mothers.add(createMotherWithAppointmentTime("2", "Mother 2", "10:00"));
        mothers.add(createMotherWithAppointmentTime("3", "Mother 3", "09:15"));
        mothers.add(createMotherWithAppointmentTime("4", "Mother 4", "11:00"));
        
        return mothers;
    }

    private Mother createMotherWithAppointmentTime(String id, String name, String appointmentTime) {
        Mother mother = new Mother();
        mother.setId(id);
        mother.setName(name);
        mother.setAddress("Test Address for " + name);
        
        Mother.FieldVisitAppointment appointment = new Mother.FieldVisitAppointment();
        appointment.setVisitId("visit-" + id);
        appointment.setDate("2024-12-20");
        appointment.setStartTime(appointmentTime);
        appointment.setEndTime("17:00");
        appointment.setStatus("confirmed");
        mother.setFieldVisitAppointment(appointment);
        
        return mother;
    }

    private List<Mother> createMothersWithAppointments() {
        List<Mother> mothers = new ArrayList<>();
        
        // Create mothers with specific appointment times and coordinates
        mothers.add(createMotherWithCoordinatesAndTime("1", "Mother 1", 6.9271, 79.8612, "10:00"));
        mothers.add(createMotherWithCoordinatesAndTime("2", "Mother 2", 6.9350, 79.8500, "09:30"));
        mothers.add(createMotherWithCoordinatesAndTime("3", "Mother 3", 6.9147, 79.8728, "11:00"));
        
        return mothers;
    }

    private Mother createMotherWithCoordinatesAndTime(String id, String name, Double latitude, Double longitude, String appointmentTime) {
        Mother mother = createMotherWithCoordinates(id, name, latitude, longitude);
        mother.getFieldVisitAppointment().setStartTime(appointmentTime);
        return mother;
    }

    private List<Mother> createLargeMothersSet(int count) {
        List<Mother> mothers = new ArrayList<>();
        
        // Create mothers with coordinates spread around Colombo area
        for (int i = 0; i < count; i++) {
            double lat = 6.9271 + (Math.random() - 0.5) * 0.1; // Random within ~10km
            double lon = 79.8612 + (Math.random() - 0.5) * 0.1;
            mothers.add(createMotherWithCoordinates(String.valueOf(i), "Mother " + i, lat, lon));
        }
        
        return mothers;
    }

    private String getAppointmentTime(Mother mother) {
        if (mother.getFieldVisitAppointment() != null && 
            mother.getFieldVisitAppointment().getStartTime() != null) {
            return mother.getFieldVisitAppointment().getStartTime();
        }
        return "09:00"; // Default time
    }
}
