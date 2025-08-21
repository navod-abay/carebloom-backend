package com.example.carebloom.services.midwife;

import com.example.carebloom.dto.midwife.FieldVisitCreateDTO;
import com.example.carebloom.dto.midwife.FieldVisitResponseDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderResponseDTO;
import com.example.carebloom.models.FieldVisit;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.FieldVisitRepository;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldVisitService {

    private static final Logger logger = LoggerFactory.getLogger(FieldVisitService.class);

    @Autowired
    private FieldVisitRepository fieldVisitRepository;

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private RouteOptimizationService routeOptimizationService;

    /**
     * Create a new field visit for a midwife
     */
    public FieldVisitResponseDTO createFieldVisit(FieldVisitCreateDTO createDTO, String firebaseUid) {
        // Find the midwife by Firebase UID
        Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        // Validate the request
        validateFieldVisitRequest(createDTO);

        // Create new field visit
        FieldVisit fieldVisit = new FieldVisit();
        fieldVisit.setMidwifeId(midwife.getId());
        fieldVisit.setDate(createDTO.getDate());
        fieldVisit.setStartTime(createDTO.getStartTime());
        fieldVisit.setEndTime(createDTO.getEndTime());
        fieldVisit.setSelectedMotherIds(createDTO.getSelectedMotherIds());
        fieldVisit.setStatus("SCHEDULED");
        fieldVisit.setCreatedAt(LocalDateTime.now());
        fieldVisit.setUpdatedAt(LocalDateTime.now());

        // Save to database
        FieldVisit savedFieldVisit = fieldVisitRepository.save(fieldVisit);

        // Update each mother's fieldVisitAppointment and collect mother info for response
        List<FieldVisitResponseDTO.MotherBasicInfo> mothers = new ArrayList<>();
        for (String motherId : createDTO.getSelectedMotherIds()) {
            Mother mother = motherRepository.findById(motherId).orElse(null);
            if (mother != null) {
                Mother.FieldVisitAppointment appointment = new Mother.FieldVisitAppointment();
                appointment.setVisitId(savedFieldVisit.getId());
                appointment.setDate(createDTO.getDate());
                appointment.setStartTime(createDTO.getStartTime());
                appointment.setEndTime(createDTO.getEndTime());
                appointment.setStatus("new");
                
                mother.setFieldVisitAppointment(appointment);
                motherRepository.save(mother);
                
                // Add to response list
                FieldVisitResponseDTO.MotherBasicInfo motherInfo = new FieldVisitResponseDTO.MotherBasicInfo();
                motherInfo.setId(motherId);
                motherInfo.setName(mother.getName());
                mothers.add(motherInfo);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found with ID: " + motherId);
            }
        }

        // Build response DTO directly
        FieldVisitResponseDTO dto = new FieldVisitResponseDTO();
        dto.setId(savedFieldVisit.getId());
        dto.setDate(savedFieldVisit.getDate());
        dto.setStartTime(savedFieldVisit.getStartTime());
        dto.setEndTime(savedFieldVisit.getEndTime());
        dto.setMidwifeId(savedFieldVisit.getMidwifeId());
        dto.setStatus(savedFieldVisit.getStatus());
        dto.setMothers(mothers);

        return dto;
    }

    /**
     * Get all field visits for a midwife
     */
    public List<FieldVisitResponseDTO> getFieldVisitsByMidwife(String firebaseUid) {
        // Find the midwife by Firebase UID
        Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
        if (midwife == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Midwife not found");
        }

        List<FieldVisit> fieldVisits = fieldVisitRepository.findByMidwifeId(midwife.getId());
        List<FieldVisitResponseDTO> responseDTOs = new ArrayList<>();
        
        for (FieldVisit fieldVisit : fieldVisits) {
            FieldVisitResponseDTO dto = new FieldVisitResponseDTO();
            dto.setId(fieldVisit.getId());
            dto.setDate(fieldVisit.getDate());
            dto.setStartTime(fieldVisit.getStartTime());
            dto.setEndTime(fieldVisit.getEndTime());
            dto.setMidwifeId(fieldVisit.getMidwifeId());
            dto.setStatus(fieldVisit.getStatus());
            
            // Get mother details
            List<FieldVisitResponseDTO.MotherBasicInfo> mothers = new ArrayList<>();
            for (String motherId : fieldVisit.getSelectedMotherIds()) {
                Mother mother = motherRepository.findById(motherId).orElse(null);
                FieldVisitResponseDTO.MotherBasicInfo motherInfo = new FieldVisitResponseDTO.MotherBasicInfo();
                motherInfo.setId(motherId);
                
                if (mother != null) {
                    motherInfo.setName(mother.getName());
                    
                    // Get appointment details from mother's fieldVisitAppointment
                    Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
                    if (appointment != null && fieldVisit.getId().equals(appointment.getVisitId())) {
                        motherInfo.setStartTime(appointment.getStartTime());
                        motherInfo.setEndTime(appointment.getEndTime());
                        motherInfo.setStatus(appointment.getStatus());
                    } else {
                        // Fallback to field visit times if no specific appointment
                        motherInfo.setStartTime(fieldVisit.getStartTime());
                        motherInfo.setEndTime(fieldVisit.getEndTime());
                        motherInfo.setStatus("new");
                    }
                } else {
                    motherInfo.setName("Unknown Mother");
                    motherInfo.setStartTime(fieldVisit.getStartTime());
                    motherInfo.setEndTime(fieldVisit.getEndTime());
                    motherInfo.setStatus("new");
                }
                mothers.add(motherInfo);
            }
            dto.setMothers(mothers);
            responseDTOs.add(dto);
        }
        
        return responseDTOs;
    }

    /**
     * Get a specific field visit by ID
     */
    public FieldVisitResponseDTO getFieldVisitById(String fieldVisitId) {
        // Get current midwife from security context
        Midwife currentMidwife = SecurityUtils.getCurrentMidwife();
        if (currentMidwife == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Midwife not found in security context");
        }

        // Find the field visit
        FieldVisit fieldVisit = fieldVisitRepository.findById(fieldVisitId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Field visit not found"));

        // Verify the field visit belongs to the current midwife
        if (!fieldVisit.getMidwifeId().equals(currentMidwife.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Field visit does not belong to current midwife");
        }

        // Build response DTO
        FieldVisitResponseDTO dto = new FieldVisitResponseDTO();
        dto.setId(fieldVisit.getId());
        dto.setDate(fieldVisit.getDate());
        dto.setStartTime(fieldVisit.getStartTime());
        dto.setEndTime(fieldVisit.getEndTime());
        dto.setMidwifeId(fieldVisit.getMidwifeId());
        dto.setStatus(fieldVisit.getStatus());

        // Get detailed mother information including appointment details
        List<FieldVisitResponseDTO.MotherBasicInfo> mothers = new ArrayList<>();
        for (String motherId : fieldVisit.getSelectedMotherIds()) {
            Mother mother = motherRepository.findById(motherId).orElse(null);
            FieldVisitResponseDTO.MotherBasicInfo motherInfo = new FieldVisitResponseDTO.MotherBasicInfo();
            motherInfo.setId(motherId);
            
            if (mother != null) {
                motherInfo.setName(mother.getName());
                
                // Get appointment details from mother's fieldVisitAppointment
                Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
                if (appointment != null && fieldVisitId.equals(appointment.getVisitId())) {
                    motherInfo.setStartTime(appointment.getStartTime());
                    motherInfo.setEndTime(appointment.getEndTime());
                    motherInfo.setStatus(appointment.getStatus());
                } else {
                    // Fallback to field visit times if no specific appointment
                    motherInfo.setStartTime(fieldVisit.getStartTime());
                    motherInfo.setEndTime(fieldVisit.getEndTime());
                    motherInfo.setStatus("new");
                }
            } else {
                motherInfo.setName("Unknown Mother");
                motherInfo.setStartTime(fieldVisit.getStartTime());
                motherInfo.setEndTime(fieldVisit.getEndTime());
                motherInfo.setStatus("new");
            }
            mothers.add(motherInfo);
        }
        dto.setMothers(mothers);

        return dto;
    }

    /**
     * Validate field visit request
     */
    private void validateFieldVisitRequest(FieldVisitCreateDTO createDTO) {
        if (createDTO.getDate() == null || createDTO.getDate().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }

        if (createDTO.getStartTime() == null || createDTO.getStartTime().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time is required");
        }

        if (createDTO.getEndTime() == null || createDTO.getEndTime().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time is required");
        }

        if (createDTO.getSelectedMotherIds() == null || createDTO.getSelectedMotherIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one mother must be selected");
        }

        // Validate time format (basic validation)
        if (!isValidTimeFormat(createDTO.getStartTime()) || !isValidTimeFormat(createDTO.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time format must be HH:MM");
        }

        // Validate date format (basic validation)
        if (!isValidDateFormat(createDTO.getDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date format must be YYYY-MM-DD");
        }
    }

    /**
     * Validate time format (HH:MM)
     */
    private boolean isValidTimeFormat(String time) {
        return time != null && time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    /**
     * Validate date format (YYYY-MM-DD)
     */
    private boolean isValidDateFormat(String date) {
        return date != null && date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    /**
     * Calculate optimal visit order for a field visit
     */
    public CalculateVisitOrderResponseDTO calculateVisitOrder(String fieldVisitId, CalculateVisitOrderDTO request) {
        // Get current midwife from security context
        Midwife currentMidwife = SecurityUtils.getCurrentMidwife();
        if (currentMidwife == null) {
            return createErrorResponse("Midwife not found in security context");
        }

        // Find the field visit
        FieldVisit fieldVisit = fieldVisitRepository.findById(fieldVisitId).orElse(null);
        if (fieldVisit == null) {
            return createErrorResponse("Field visit not found");
        }

        // Verify the field visit belongs to the current midwife
        if (!fieldVisit.getMidwifeId().equals(currentMidwife.getId())) {
            return createErrorResponse("Access denied: Field visit does not belong to current midwife");
        }

        // Get eligible mothers based on overrideUnconfirmed flag
        List<Mother> eligibleMothers = getEligibleMothers(fieldVisit, request.getOverrideUnconfirmed());
        
        if (eligibleMothers.isEmpty()) {
            return createErrorResponse("No eligible mothers found for route calculation");
        }

        // Check if all mothers have location data
        List<Mother> mothersWithLocation = eligibleMothers.stream()
            .filter(mother -> routeOptimizationService.hasValidCoordinates(mother))
            .collect(Collectors.toList());

        if (mothersWithLocation.isEmpty()) {
            // If no mothers have coordinates, use address-based simple ordering
            mothersWithLocation = eligibleMothers.stream()
                .filter(mother -> hasValidLocation(mother))
                .collect(Collectors.toList());
                
            if (mothersWithLocation.isEmpty()) {
                return createErrorResponse("No mothers with valid location data found");
            }
            
            logger.warn("No mothers with coordinates found, using address-based ordering");
            // Use simple time-based ordering as fallback
            List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = calculateSimpleTimeBasedOrder(
                mothersWithLocation, fieldVisit);
            return createSuccessResponse(visitOrder);
        }

        if (mothersWithLocation.size() < eligibleMothers.size()) {
            logger.warn("Some mothers missing coordinates. Processing {} out of {} mothers", 
                       mothersWithLocation.size(), eligibleMothers.size());
        }

        // Use RouteOptimizationService for optimal routing
        try {
            LocalTime startTime = LocalTime.parse(fieldVisit.getStartTime());
            LocalTime endTime = LocalTime.parse(fieldVisit.getEndTime());
            
            List<Mother> optimizedOrder = routeOptimizationService.optimizeVisitOrder(
                mothersWithLocation, startTime, endTime);
            
            List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = 
                convertToVisitOrderResponse(optimizedOrder, fieldVisit);
            
            return createSuccessResponse(visitOrder);
            
        } catch (Exception e) {
            logger.error("Error during route optimization, falling back to simple ordering", e);
            List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = calculateSimpleTimeBasedOrder(
                mothersWithLocation, fieldVisit);
            return createSuccessResponse(visitOrder);
        }
    }

    /**
     * Get eligible mothers for route calculation
     */
    private List<Mother> getEligibleMothers(FieldVisit fieldVisit, Boolean overrideUnconfirmed) {
        List<Mother> eligibleMothers = new ArrayList<>();
        
        for (String motherId : fieldVisit.getSelectedMotherIds()) {
            Mother mother = motherRepository.findById(motherId).orElse(null);
            if (mother == null) continue;

            Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
            if (appointment == null || !fieldVisit.getId().equals(appointment.getVisitId())) {
                continue;
            }

            // Include mother based on status and override flag
            String status = appointment.getStatus();
            boolean shouldInclude = "confirmed".equals(status) || 
                                  ("new".equals(status) && Boolean.TRUE.equals(overrideUnconfirmed));
            
            if (shouldInclude) {
                eligibleMothers.add(mother);
            }
        }
        
        return eligibleMothers;
    }

    /**
     * Check if mother has valid location data
     */
    private boolean hasValidLocation(Mother mother) {
        return mother.getAddress() != null && !mother.getAddress().trim().isEmpty();
    }

    /**
     * Simple time-based ordering (placeholder for VRPTW algorithm)
     */
    private List<CalculateVisitOrderResponseDTO.VisitOrder> calculateSimpleTimeBasedOrder(
            List<Mother> mothers, FieldVisit fieldVisit) {
        
        List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = new ArrayList<>();
        String currentTime = fieldVisit.getStartTime();
        
        // Sort mothers by their confirmed start time or use field visit start time
        mothers.sort((m1, m2) -> {
            String time1 = getMotherStartTime(m1, fieldVisit);
            String time2 = getMotherStartTime(m2, fieldVisit);
            return time1.compareTo(time2);
        });

        for (int i = 0; i < mothers.size(); i++) {
            Mother mother = mothers.get(i);
            CalculateVisitOrderResponseDTO.VisitOrder order = new CalculateVisitOrderResponseDTO.VisitOrder();
            
            order.setMotherId(mother.getId());
            order.setMotherName(mother.getName());
            order.setAddress(mother.getAddress());
            order.setEstimatedArrivalTime(currentTime);
            order.setEstimatedDuration(30); // Default 30 minutes per visit
            order.setDistance(i == 0 ? 0.0 : 1000.0 * (i)); // 1km between visits as placeholder
            
            visitOrder.add(order);
            currentTime = addMinutesToTime(currentTime, 30);
        }
        
        return visitOrder;
    }

    /**
     * Get mother's start time from appointment or fallback to field visit time
     */
    private String getMotherStartTime(Mother mother, FieldVisit fieldVisit) {
        Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
        if (appointment != null && appointment.getStartTime() != null) {
            return appointment.getStartTime();
        }
        return fieldVisit.getStartTime();
    }

    /**
     * Add minutes to time string (HH:MM format)
     */
    private String addMinutesToTime(String time, int minutes) {
        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int mins = Integer.parseInt(parts[1]);
            
            mins += minutes;
            hours += mins / 60;
            mins = mins % 60;
            
            return String.format("%02d:%02d", hours, mins);
        } catch (Exception e) {
            logger.error("Error adding minutes to time: {}", time, e);
            return time;
        }
    }

    /**
     * Create error response
     */
    private CalculateVisitOrderResponseDTO createErrorResponse(String message) {
        CalculateVisitOrderResponseDTO response = new CalculateVisitOrderResponseDTO();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    /**
     * Create success response with calculated visit order
     */
    private CalculateVisitOrderResponseDTO createSuccessResponse(List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder) {
        // Calculate totals
        double totalDistance = visitOrder.stream()
            .mapToDouble(order -> order.getDistance() != null ? order.getDistance() : 0.0)
            .sum();
        
        int totalTime = visitOrder.stream()
            .mapToInt(order -> order.getEstimatedDuration() != null ? order.getEstimatedDuration() : 30)
            .sum();

        CalculateVisitOrderResponseDTO response = new CalculateVisitOrderResponseDTO();
        response.setSuccess(true);
        response.setMessage("Visit order calculated successfully using route optimization");
        response.setVisitOrder(visitOrder);
        response.setTotalDistance(totalDistance);
        response.setTotalEstimatedTime(totalTime);

        logger.info("Visit order calculated successfully: {} mothers, total distance: {}m, total time: {}min", 
                   visitOrder.size(), totalDistance, totalTime);
        return response;
    }

    /**
     * Convert optimized mother list to visit order response format
     */
    private List<CalculateVisitOrderResponseDTO.VisitOrder> convertToVisitOrderResponse(
            List<Mother> optimizedMothers, FieldVisit fieldVisit) {
        
        List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = new ArrayList<>();
        String currentTime = fieldVisit.getStartTime();
        
        for (int i = 0; i < optimizedMothers.size(); i++) {
            Mother mother = optimizedMothers.get(i);
            CalculateVisitOrderResponseDTO.VisitOrder order = new CalculateVisitOrderResponseDTO.VisitOrder();
            
            order.setMotherId(mother.getId());
            order.setMotherName(mother.getName());
            order.setAddress(mother.getLocationAddress() != null ? mother.getLocationAddress() : mother.getAddress());
            order.setEstimatedArrivalTime(currentTime);
            order.setEstimatedDuration(30); // Default 30 minutes per visit
            
            // Calculate distance from previous location (simplified)
            if (i == 0) {
                order.setDistance(0.0); // Starting point
            } else {
                Mother prevMother = optimizedMothers.get(i - 1);
                if (routeOptimizationService.hasValidCoordinates(mother) && 
                    routeOptimizationService.hasValidCoordinates(prevMother)) {
                    // Use Haversine distance calculation if coordinates available
                    double distance = calculateHaversineDistance(
                        prevMother.getLatitude(), prevMother.getLongitude(),
                        mother.getLatitude(), mother.getLongitude()
                    );
                    order.setDistance(distance);
                } else {
                    order.setDistance(1000.0 * i); // Fallback: 1km per step
                }
            }
            
            visitOrder.add(order);
            currentTime = addMinutesToTime(currentTime, 30);
        }
        
        return visitOrder;
    }

    /**
     * Calculate Haversine distance between two coordinates
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
