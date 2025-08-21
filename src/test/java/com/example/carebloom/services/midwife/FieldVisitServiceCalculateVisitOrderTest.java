package com.example.carebloom.services.midwife;

import com.example.carebloom.dto.midwife.CalculateVisitOrderDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderResponseDTO;
import com.example.carebloom.models.FieldVisit;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.FieldVisitRepository;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FieldVisitServiceCalculateVisitOrderTest {

    @Mock
    private FieldVisitRepository fieldVisitRepository;

    @Mock
    private MidwifeRepository midwifeRepository;

    @Mock
    private MotherRepository motherRepository;

    @Mock
    private RouteOptimizationService routeOptimizationService;

    @InjectMocks
    private FieldVisitService fieldVisitService;

    private Midwife testMidwife;
    private FieldVisit testFieldVisit;
    private List<Mother> testMothers;
    private CalculateVisitOrderDTO testRequest;

    @BeforeEach
    void setUp() {
        testMidwife = createTestMidwife();
        testFieldVisit = createTestFieldVisit();
        testMothers = createTestMothers();
        testRequest = new CalculateVisitOrderDTO();
        testRequest.setOverrideUnconfirmed(true);
    }

    @Test
    void testCalculateVisitOrder_Success_WithCoordinates() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(testMidwife);
            
            when(fieldVisitRepository.findById("visit-1")).thenReturn(Optional.of(testFieldVisit));
            when(motherRepository.findById("mother-1")).thenReturn(Optional.of(testMothers.get(0)));
            when(motherRepository.findById("mother-2")).thenReturn(Optional.of(testMothers.get(1)));
            
            when(routeOptimizationService.hasValidCoordinates(any(Mother.class))).thenReturn(true);
            when(routeOptimizationService.optimizeVisitOrder(anyList(), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(testMothers);

        // When
        CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Visit order calculated successfully using route optimization", result.getMessage());
        assertNotNull(result.getVisitOrder());
        assertEquals(2, result.getVisitOrder().size());
        assertTrue(result.getTotalDistance() >= 0);
        assertTrue(result.getTotalEstimatedTime() > 0);            verify(routeOptimizationService).optimizeVisitOrder(anyList(), any(LocalTime.class), any(LocalTime.class));
        }
    }

    @Test
    void testCalculateVisitOrder_Success_WithoutCoordinates_FallbackToAddressBased() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(testMidwife);
            
            when(fieldVisitRepository.findById("visit-1")).thenReturn(Optional.of(testFieldVisit));
            when(motherRepository.findById("mother-1")).thenReturn(Optional.of(testMothers.get(0)));
            when(motherRepository.findById("mother-2")).thenReturn(Optional.of(testMothers.get(1)));
            
            // No mothers have valid coordinates
            when(routeOptimizationService.hasValidCoordinates(any(Mother.class))).thenReturn(false);

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Visit order calculated successfully using route optimization", result.getMessage());
            assertNotNull(result.getVisitOrder());
            assertEquals(2, result.getVisitOrder().size());

            // Should not call route optimization service
            verify(routeOptimizationService, never()).optimizeVisitOrder(anyList(), any(LocalTime.class), any(LocalTime.class));
        }
    }

    @Test
    void testCalculateVisitOrder_MidwifeNotFound_ReturnsError() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(null);

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

            // Then
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("Midwife not found in security context", result.getMessage());
            assertNull(result.getVisitOrder());
        }
    }

    @Test
    void testCalculateVisitOrder_FieldVisitNotFound_ReturnsError() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(testMidwife);
            when(fieldVisitRepository.findById("nonexistent")).thenReturn(Optional.empty());

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("nonexistent", testRequest);

            // Then
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("Field visit not found", result.getMessage());
        }
    }

    @Test
    void testCalculateVisitOrder_AccessDenied_WrongMidwife_ReturnsError() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            Midwife wrongMidwife = new Midwife();
            wrongMidwife.setId("wrong-midwife-id");
            wrongMidwife.setFirebaseUid("wrong-uid");
            
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(wrongMidwife);
            when(fieldVisitRepository.findById("visit-1")).thenReturn(Optional.of(testFieldVisit));

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

            // Then
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("Access denied: Field visit does not belong to current midwife", result.getMessage());
        }
    }

    @Test
    void testCalculateVisitOrder_NoEligibleMothers_ReturnsError() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(testMidwife);
            
            FieldVisit fieldVisitWithNoMothers = new FieldVisit();
            fieldVisitWithNoMothers.setId("visit-1");
            fieldVisitWithNoMothers.setMidwifeId("midwife-1");
            fieldVisitWithNoMothers.setSelectedMotherIds(new ArrayList<>());
            
            when(fieldVisitRepository.findById("visit-1")).thenReturn(Optional.of(fieldVisitWithNoMothers));

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

            // Then
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("No eligible mothers found for route calculation", result.getMessage());
        }
    }

    @Test
    void testCalculateVisitOrder_OverrideUnconfirmedFalse_OnlyConfirmedMothers() {
        // Given
        testRequest.setOverrideUnconfirmed(false);
        
        // Create mothers with different statuses
        Mother confirmedMother = testMothers.get(0);
        confirmedMother.getFieldVisitAppointment().setStatus("confirmed");
        
        Mother newMother = testMothers.get(1);
        newMother.getFieldVisitAppointment().setStatus("new");
        
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(testMidwife);
            
            when(fieldVisitRepository.findById("visit-1")).thenReturn(Optional.of(testFieldVisit));
            when(motherRepository.findById("mother-1")).thenReturn(Optional.of(confirmedMother));
            when(motherRepository.findById("mother-2")).thenReturn(Optional.of(newMother));
            
            when(routeOptimizationService.hasValidCoordinates(any(Mother.class))).thenReturn(false);

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getVisitOrder().size()); // Only confirmed mother
            assertEquals("mother-1", result.getVisitOrder().get(0).getMotherId());
        }
    }

    @Test
    void testCalculateVisitOrder_RouteOptimizationException_FallsBackToSimpleOrdering() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(testMidwife);
            
            when(fieldVisitRepository.findById("visit-1")).thenReturn(Optional.of(testFieldVisit));
            when(motherRepository.findById("mother-1")).thenReturn(Optional.of(testMothers.get(0)));
            when(motherRepository.findById("mother-2")).thenReturn(Optional.of(testMothers.get(1)));
            
            when(routeOptimizationService.hasValidCoordinates(any(Mother.class))).thenReturn(true);
            when(routeOptimizationService.optimizeVisitOrder(anyList(), any(LocalTime.class), any(LocalTime.class)))
                .thenThrow(new RuntimeException("OR-Tools optimization failed"));

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Visit order calculated successfully using route optimization", result.getMessage());
            assertNotNull(result.getVisitOrder());
            assertEquals(2, result.getVisitOrder().size());

            verify(routeOptimizationService).optimizeVisitOrder(anyList(), any(LocalTime.class), any(LocalTime.class));
        }
    }

    @Test
    void testCalculateVisitOrder_NoValidLocationData_ReturnsError() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentMidwife).thenReturn(testMidwife);
            
            // Create mothers without address or coordinates
            Mother motherWithoutLocation1 = new Mother();
            motherWithoutLocation1.setId("mother-1");
            motherWithoutLocation1.setName("Mother 1");
            motherWithoutLocation1.setAddress(null);
            Mother.FieldVisitAppointment appointment1 = new Mother.FieldVisitAppointment();
            appointment1.setVisitId("visit-1");
            appointment1.setStatus("confirmed");
            motherWithoutLocation1.setFieldVisitAppointment(appointment1);
            
            Mother motherWithoutLocation2 = new Mother();
            motherWithoutLocation2.setId("mother-2");
            motherWithoutLocation2.setName("Mother 2");
            motherWithoutLocation2.setAddress("");
            Mother.FieldVisitAppointment appointment2 = new Mother.FieldVisitAppointment();
            appointment2.setVisitId("visit-1");
            appointment2.setStatus("confirmed");
            motherWithoutLocation2.setFieldVisitAppointment(appointment2);
            
            when(fieldVisitRepository.findById("visit-1")).thenReturn(Optional.of(testFieldVisit));
            when(motherRepository.findById("mother-1")).thenReturn(Optional.of(motherWithoutLocation1));
            when(motherRepository.findById("mother-2")).thenReturn(Optional.of(motherWithoutLocation2));
            
            when(routeOptimizationService.hasValidCoordinates(any(Mother.class))).thenReturn(false);

            // When
            CalculateVisitOrderResponseDTO result = fieldVisitService.calculateVisitOrder("visit-1", testRequest);

            // Then
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("No mothers with valid location data found", result.getMessage());
        }
    }

    // Helper methods

    private Midwife createTestMidwife() {
        Midwife midwife = new Midwife();
        midwife.setId("midwife-1");
        midwife.setFirebaseUid("midwife-firebase-uid");
        midwife.setName("Test Midwife");
        return midwife;
    }

    private FieldVisit createTestFieldVisit() {
        FieldVisit fieldVisit = new FieldVisit();
        fieldVisit.setId("visit-1");
        fieldVisit.setMidwifeId("midwife-1");
        fieldVisit.setDate("2024-12-20");
        fieldVisit.setStartTime("09:00");
        fieldVisit.setEndTime("17:00");
        fieldVisit.setSelectedMotherIds(List.of("mother-1", "mother-2"));
        fieldVisit.setStatus("SCHEDULED");
        return fieldVisit;
    }

    private List<Mother> createTestMothers() {
        List<Mother> mothers = new ArrayList<>();
        
        // Mother 1 with coordinates
        Mother mother1 = new Mother();
        mother1.setId("mother-1");
        mother1.setName("Mother 1");
        mother1.setLatitude(6.9271);
        mother1.setLongitude(79.8612);
        mother1.setAddress("123 Main Street, Colombo");
        mother1.setLocationAddress("GPS Location 1");
        
        Mother.FieldVisitAppointment appointment1 = new Mother.FieldVisitAppointment();
        appointment1.setVisitId("visit-1");
        appointment1.setDate("2024-12-20");
        appointment1.setStartTime("10:00");
        appointment1.setEndTime("10:30");
        appointment1.setStatus("confirmed");
        mother1.setFieldVisitAppointment(appointment1);
        
        mothers.add(mother1);
        
        // Mother 2 with coordinates
        Mother mother2 = new Mother();
        mother2.setId("mother-2");
        mother2.setName("Mother 2");
        mother2.setLatitude(6.9350);
        mother2.setLongitude(79.8500);
        mother2.setAddress("456 Second Street, Colombo 7");
        mother2.setLocationAddress("GPS Location 2");
        
        Mother.FieldVisitAppointment appointment2 = new Mother.FieldVisitAppointment();
        appointment2.setVisitId("visit-1");
        appointment2.setDate("2024-12-20");
        appointment2.setStartTime("09:30");
        appointment2.setEndTime("10:00");
        appointment2.setStatus("confirmed");
        mother2.setFieldVisitAppointment(appointment2);
        
        mothers.add(mother2);
        
        return mothers;
    }
}
