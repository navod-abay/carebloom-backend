package com.example.carebloom.services.midwife;

import com.example.carebloom.dto.midwife.FieldVisitCreateDTO;
import com.example.carebloom.dto.midwife.FieldVisitResponseDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderDTO;
import com.example.carebloom.dto.midwife.CalculateVisitOrderResponseDTO;
import com.example.carebloom.dto.navigation.NavigationLocation;
import com.example.carebloom.dto.navigation.TravelTimeResult;
import com.example.carebloom.models.FieldVisit;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.FieldVisitRepository;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.services.navigation.GoogleMapsDistanceService;
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

    @Autowired
    private GoogleMapsDistanceService googleMapsDistanceService;

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

        // Build response DTO with enhanced schedule information
        FieldVisitResponseDTO dto = new FieldVisitResponseDTO();
        dto.setId(fieldVisit.getId());
        dto.setDate(fieldVisit.getDate());
        dto.setStartTime(fieldVisit.getStartTime());
        dto.setEndTime(fieldVisit.getEndTime());
        dto.setMidwifeId(fieldVisit.getMidwifeId());
        dto.setStatus(fieldVisit.getStatus());
        dto.setCreatedAt(fieldVisit.getCreatedAt());
        dto.setUpdatedAt(fieldVisit.getUpdatedAt());

        // Convert enhanced schedule information if available
        if (fieldVisit.getSchedule() != null) {
            dto.setSchedule(convertToRouteScheduleInfo(fieldVisit.getSchedule()));
        }

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
        logger.info("=== CALCULATE VISIT ORDER STARTED ===");
        logger.info("Field Visit ID: {}", fieldVisitId);
        logger.info("Override Unconfirmed: {}", request.getOverrideUnconfirmed());
        
        // Get current midwife from security context
        Midwife currentMidwife = SecurityUtils.getCurrentMidwife();
        if (currentMidwife == null) {
            logger.error("ERROR: Midwife not found in security context");
            return createErrorResponse("Midwife not found in security context");
        }
        logger.info("Current midwife: {} (ID: {})", currentMidwife.getName(), currentMidwife.getId());

        // Find the field visit
        FieldVisit fieldVisit = fieldVisitRepository.findById(fieldVisitId).orElse(null);
        if (fieldVisit == null) {
            logger.error("ERROR: Field visit not found with ID: {}", fieldVisitId);
            return createErrorResponse("Field visit not found");
        }
        logger.info("Field visit found: Date {}, Time {}-{}, Status: {}", 
                   fieldVisit.getDate(), fieldVisit.getStartTime(), fieldVisit.getEndTime(), fieldVisit.getStatus());

        // Verify the field visit belongs to the current midwife
        if (!fieldVisit.getMidwifeId().equals(currentMidwife.getId())) {
            logger.error("ERROR: Access denied - Field visit belongs to midwife ID: {}, current midwife ID: {}", 
                        fieldVisit.getMidwifeId(), currentMidwife.getId());
            return createErrorResponse("Access denied: Field visit does not belong to current midwife");
        }
        logger.info("Access verified: Field visit belongs to current midwife");

        // Get eligible mothers based on overrideUnconfirmed flag
        logger.info("--- STEP 1: Getting eligible mothers ---");
        List<Mother> eligibleMothers = getEligibleMothers(fieldVisit, request.getOverrideUnconfirmed());
        logger.info("Found {} eligible mothers out of {} selected mothers", 
                   eligibleMothers.size(), fieldVisit.getSelectedMotherIds().size());
        
        if (eligibleMothers.isEmpty()) {
            logger.warn("WARNING: No eligible mothers found for route calculation");
            return createErrorResponse("No eligible mothers found for route calculation");
        }

        // Log eligible mothers
        logger.info("Eligible mothers:");
        for (int i = 0; i < eligibleMothers.size(); i++) {
            Mother m = eligibleMothers.get(i);
            String appointmentStatus = m.getFieldVisitAppointment() != null ? 
                m.getFieldVisitAppointment().getStatus() : "N/A";
            logger.info("  {}. {} (lat: {}, lon: {}, status: {})", 
                       i + 1, m.getName(), m.getLatitude(), m.getLongitude(), appointmentStatus);
        }

        logger.info("--- STEP 2: Checking location data ---");
        // Check if all mothers have location data
        List<Mother> mothersWithLocation = eligibleMothers.stream()
            .filter(mother -> routeOptimizationService.hasValidCoordinates(mother))
            .collect(Collectors.toList());

        logger.info("Location validation: {}/{} mothers have valid coordinates", 
                   mothersWithLocation.size(), eligibleMothers.size());

        if (mothersWithLocation.isEmpty()) {
            // If no mothers have coordinates, use address-based simple ordering
            mothersWithLocation = eligibleMothers.stream()
                .filter(mother -> hasValidLocation(mother))
                .collect(Collectors.toList());
                
            if (mothersWithLocation.isEmpty()) {
                logger.error("ERROR: No mothers with valid location data found");
                return createErrorResponse("No mothers with valid location data found");
            }
            
            logger.warn("No mothers with coordinates found, using address-based ordering for {} mothers", 
                       mothersWithLocation.size());
            // Use simple time-based ordering as fallback
            List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = calculateSimpleTimeBasedOrder(
                mothersWithLocation, fieldVisit);
            return createSuccessResponse(visitOrder, fieldVisit);
        }

        if (mothersWithLocation.size() < eligibleMothers.size()) {
            logger.warn("WARNING: Some mothers missing coordinates. Processing {} out of {} mothers", 
                       mothersWithLocation.size(), eligibleMothers.size());
            
            // Log mothers without coordinates
            eligibleMothers.stream()
                .filter(m -> !routeOptimizationService.hasValidCoordinates(m))
                .forEach(m -> logger.warn("  Missing coordinates: {} (lat: {}, lon: {})", 
                               m.getName(), m.getLatitude(), m.getLongitude()));
        }

        logger.info("--- STEP 3: Calling enhanced route optimization service ---");
        // Use RouteOptimizationService for optimal routing with metrics
        try {
            LocalTime startTime = LocalTime.parse(fieldVisit.getStartTime());
            LocalTime endTime = LocalTime.parse(fieldVisit.getEndTime());
            logger.info("Optimization time window: {} - {}", startTime, endTime);
            
            RouteOptimizationService.OptimizationResult result = routeOptimizationService.optimizeVisitOrderWithMetrics(
                mothersWithLocation, startTime, endTime);
            
            logger.info("--- STEP 4: Converting to response format ---");
            List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = 
                convertToVisitOrderResponse(result.getOptimizedOrder(), fieldVisit);
            
            logger.info("--- STEP 5: Creating success response with enhanced persistence ---");
            CalculateVisitOrderResponseDTO response = createSuccessResponseWithMetrics(visitOrder, fieldVisit, result);
            
            logger.info("=== CALCULATE VISIT ORDER COMPLETED SUCCESSFULLY ===");
            logger.info("Final visit order: {} mothers, total distance: {}m, total time: {}min, algorithm fallback: {}", 
                       response.getVisitOrder().size(), response.getTotalDistance(), response.getTotalEstimatedTime(), result.isFellbackToSimple());
            
            return response;
            
        } catch (Exception e) {
            logger.error("ERROR during route optimization: {}", e.getMessage(), e);
            logger.warn("Falling back to simple ordering due to optimization error");
            List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder = calculateSimpleTimeBasedOrder(
                mothersWithLocation, fieldVisit);
            return createSuccessResponse(visitOrder, fieldVisit);
        }
    }

    /**
     * Get eligible mothers for route calculation
     */
    private List<Mother> getEligibleMothers(FieldVisit fieldVisit, Boolean overrideUnconfirmed) {
        logger.info("Getting eligible mothers from {} selected mother IDs", fieldVisit.getSelectedMotherIds().size());
        logger.info("Override unconfirmed appointments: {}", overrideUnconfirmed);
        
        List<Mother> eligibleMothers = new ArrayList<>();
        int foundMothers = 0;
        int missingMothers = 0;
        int confirmedCount = 0;
        int newCount = 0;
        int otherStatusCount = 0;
        
        for (String motherId : fieldVisit.getSelectedMotherIds()) {
            logger.debug("Processing mother ID: {}", motherId);
            Mother mother = motherRepository.findById(motherId).orElse(null);
            if (mother == null) {
                missingMothers++;
                logger.warn("Mother not found with ID: {}", motherId);
                continue;
            }
            foundMothers++;

            Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
            if (appointment == null) {
                logger.warn("Mother {} has no field visit appointment", mother.getName());
                continue;
            }
            
            if (!fieldVisit.getId().equals(appointment.getVisitId())) {
                logger.warn("Mother {} appointment visit ID ({}) doesn't match field visit ID ({})", 
                           mother.getName(), appointment.getVisitId(), fieldVisit.getId());
                continue;
            }

            // Include mother based on status and override flag
            String status = appointment.getStatus();
            boolean shouldInclude = false;
            
            if ("confirmed".equals(status)) {
                confirmedCount++;
                shouldInclude = true;
                logger.debug("Including {} - confirmed appointment", mother.getName());
            } else if ("new".equals(status) && Boolean.TRUE.equals(overrideUnconfirmed)) {
                newCount++;
                shouldInclude = true;
                logger.debug("Including {} - new appointment with override", mother.getName());
            } else if ("new".equals(status)) {
                logger.debug("Excluding {} - new appointment without override", mother.getName());
            } else {
                otherStatusCount++;
                logger.debug("Excluding {} - status: {}", mother.getName(), status);
            }
            
            if (shouldInclude) {
                eligibleMothers.add(mother);
            }
        }
        
        logger.info("Eligible mothers summary:");
        logger.info("  Total selected: {}", fieldVisit.getSelectedMotherIds().size());
        logger.info("  Found in database: {}", foundMothers);
        logger.info("  Missing from database: {}", missingMothers);
        logger.info("  Confirmed appointments: {}", confirmedCount);
        logger.info("  New appointments: {}", newCount);
        logger.info("  Other status: {}", otherStatusCount);
        logger.info("  Final eligible count: {}", eligibleMothers.size());
        
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
            order.setLatitude(mother.getLatitude());
            order.setLongitude(mother.getLongitude());
            
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
     * Create success response with calculated visit order and persist the order
     */
    private CalculateVisitOrderResponseDTO createSuccessResponse(List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder, FieldVisit fieldVisit) {
        // Calculate totals
        double totalDistance = visitOrder.stream()
            .mapToDouble(order -> order.getDistance() != null ? order.getDistance() : 0.0)
            .sum();
        
        int totalTime = visitOrder.stream()
            .mapToInt(order -> order.getEstimatedDuration() != null ? order.getEstimatedDuration() : 30)
            .sum();

        // Persist the calculated order to database
        persistVisitOrder(visitOrder, fieldVisit);

        CalculateVisitOrderResponseDTO response = new CalculateVisitOrderResponseDTO();
        response.setSuccess(true);
        response.setMessage("Visit order calculated and saved successfully");
        response.setVisitOrder(visitOrder);
        response.setTotalDistance(totalDistance);
        response.setTotalEstimatedTime(totalTime);

        logger.info("Visit order calculated and persisted successfully: {} mothers, total distance: {}m, total time: {}min", 
                   visitOrder.size(), totalDistance, totalTime);
        return response;
    }

    /**
     * Create success response with enhanced metrics and persistence
     */
    private CalculateVisitOrderResponseDTO createSuccessResponseWithMetrics(List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder, 
                                                                           FieldVisit fieldVisit, 
                                                                           RouteOptimizationService.OptimizationResult optimizationResult) {
        // Calculate totals
        double totalDistance = visitOrder.stream()
            .mapToDouble(order -> order.getDistance() != null ? order.getDistance() : 0.0)
            .sum();
        
        int totalTime = visitOrder.stream()
            .mapToInt(order -> order.getEstimatedDuration() != null ? order.getEstimatedDuration() : 30)
            .sum();

        // Create enhanced schedule and persist to database
        FieldVisit.RouteSchedule schedule = createRouteSchedule(visitOrder, fieldVisit);
        
        // Update metadata with optimization result
        schedule.getMetadata().setFellbackToSimple(optimizationResult.isFellbackToSimple());
        
        // Persist the calculated order with enhanced schedule
        persistVisitOrderWithSchedule(visitOrder, fieldVisit, schedule);

        CalculateVisitOrderResponseDTO response = new CalculateVisitOrderResponseDTO();
        response.setSuccess(true);
        response.setMessage("Visit order calculated successfully using " + 
                           (optimizationResult.isFellbackToSimple() ? "simple algorithm" : "route optimization"));
        response.setVisitOrder(visitOrder);
        response.setTotalDistance(totalDistance);
        response.setTotalEstimatedTime(totalTime);

        logger.info("Enhanced visit order calculated and persisted successfully: {} mothers, total distance: {}m, total time: {}min, fallback: {}", 
                   visitOrder.size(), totalDistance, totalTime, optimizationResult.isFellbackToSimple());
        return response;
    }

    /**
     * Persist visit order with pre-created schedule object
     */
    private void persistVisitOrderWithSchedule(List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder, 
                                              FieldVisit fieldVisit, 
                                              FieldVisit.RouteSchedule schedule) {
        logger.info("=== PERSISTING ENHANCED VISIT ORDER WITH SCHEDULE TO DATABASE ===");
        
        try {
            // Update FieldVisit with schedule and reorder selectedMotherIds
            List<String> orderedMotherIds = visitOrder.stream()
                .map(CalculateVisitOrderResponseDTO.VisitOrder::getMotherId)
                .collect(Collectors.toList());
            
            fieldVisit.setSelectedMotherIds(orderedMotherIds);
            fieldVisit.setSchedule(schedule);
            fieldVisit.setStatus("CALCULATED");
            fieldVisit.setUpdatedAt(LocalDateTime.now());
            fieldVisitRepository.save(fieldVisit);
            
            // Update mother appointments with scheduled times
            updateMotherScheduledTimes(visitOrder, fieldVisit);
            
            logger.info("Enhanced visit order persistence completed successfully");
            
        } catch (Exception e) {
            logger.error("ERROR persisting enhanced visit order: {}", e.getMessage(), e);
        }
    }

    /**
     * Persist the calculated visit order to database with enhanced schedule data
     */
    private void persistVisitOrder(List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder, FieldVisit fieldVisit) {
        logger.info("=== PERSISTING ENHANCED VISIT ORDER TO DATABASE ===");
        logger.info("Persisting visit order for {} mothers in field visit {}", visitOrder.size(), fieldVisit.getId());
        
        try {
            // 1. Create enhanced RouteSchedule object
            FieldVisit.RouteSchedule schedule = createRouteSchedule(visitOrder, fieldVisit);
            
            // 2. Update FieldVisit with schedule and reorder selectedMotherIds
            List<String> orderedMotherIds = visitOrder.stream()
                .map(CalculateVisitOrderResponseDTO.VisitOrder::getMotherId)
                .collect(Collectors.toList());
            
            logger.info("Updating FieldVisit status and reordering mother IDs");
            logger.info("Original order: {}", fieldVisit.getSelectedMotherIds());
            logger.info("Optimized order: {}", orderedMotherIds);
            
            fieldVisit.setSelectedMotherIds(orderedMotherIds);
            fieldVisit.setSchedule(schedule);
            fieldVisit.setStatus("CALCULATED");
            fieldVisit.setUpdatedAt(LocalDateTime.now());
            fieldVisitRepository.save(fieldVisit);
            
            logger.info("Updated FieldVisit {} with enhanced schedule data", fieldVisit.getId());

            // 3. Update each Mother's FieldVisitAppointment with scheduled times (preserve originals)
            updateMotherScheduledTimes(visitOrder, fieldVisit);
            
            logger.info("=== ENHANCED VISIT ORDER PERSISTENCE COMPLETED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            logger.error("ERROR persisting enhanced visit order for FieldVisit {}: {}", fieldVisit.getId(), e.getMessage(), e);
            logger.error("Persistence failed, but calculated order will still be returned to user");
        }
    }

    /**
     * Create enhanced RouteSchedule object with comprehensive data
     */
    private FieldVisit.RouteSchedule createRouteSchedule(List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder, FieldVisit fieldVisit) {
        List<FieldVisit.RouteSchedule.ScheduledVisit> scheduledVisits = new ArrayList<>();
        double totalDistance = 0;
        int totalTravelTime = 0;
        int totalServiceTime = 0;
        
        for (int i = 0; i < visitOrder.size(); i++) {
            CalculateVisitOrderResponseDTO.VisitOrder order = visitOrder.get(i);
            Mother mother = motherRepository.findById(order.getMotherId()).orElse(null);
            
            FieldVisit.RouteSchedule.ScheduledVisit scheduledVisit = new FieldVisit.RouteSchedule.ScheduledVisit();
            scheduledVisit.setMotherId(order.getMotherId());
            scheduledVisit.setMotherName(order.getMotherName());
            scheduledVisit.setVisitOrder(i + 1);
            scheduledVisit.setScheduledStartTime(order.getEstimatedArrivalTime());
            scheduledVisit.setScheduledEndTime(addMinutesToTime(order.getEstimatedArrivalTime(), order.getEstimatedDuration()));
            scheduledVisit.setEstimatedDuration(order.getEstimatedDuration());
            scheduledVisit.setDistanceFromPrevious(order.getDistance());
            scheduledVisit.setTravelTimeFromPrevious(order.getTravelTimeMinutes());
            
            // Preserve original times from mother's appointment
            if (mother != null && mother.getFieldVisitAppointment() != null) {
                scheduledVisit.setOriginalStartTime(mother.getFieldVisitAppointment().getStartTime());
                scheduledVisit.setOriginalEndTime(mother.getFieldVisitAppointment().getEndTime());
            }
            
            // Set coordinates
            FieldVisit.RouteSchedule.ScheduledVisit.RouteCoordinates coords = new FieldVisit.RouteSchedule.ScheduledVisit.RouteCoordinates();
            if (mother != null) {
                coords.setLatitude(mother.getLatitude());
                coords.setLongitude(mother.getLongitude());
                coords.setAddress(mother.getLocationAddress() != null ? mother.getLocationAddress() : mother.getAddress());
            }
            scheduledVisit.setCoordinates(coords);
            
            scheduledVisits.add(scheduledVisit);
            
            // Accumulate metrics
            if (order.getDistance() != null) totalDistance += order.getDistance();
            if (order.getTravelTimeMinutes() != null) totalTravelTime += order.getTravelTimeMinutes();
            if (order.getEstimatedDuration() != null) totalServiceTime += order.getEstimatedDuration();
        }
        
        // Create metadata (simplified per requirements)
        FieldVisit.RouteSchedule.ScheduleMetadata metadata = new FieldVisit.RouteSchedule.ScheduleMetadata();
        metadata.setTotalDistance(totalDistance);
        metadata.setTotalTravelTime(totalTravelTime);
        metadata.setTotalServiceTime(totalServiceTime);
        metadata.setFellbackToSimple(false); // Will be set by calling method
        
        // Create schedule
        FieldVisit.RouteSchedule schedule = new FieldVisit.RouteSchedule();
        schedule.setScheduledVisits(scheduledVisits);
        schedule.setMetadata(metadata);
        schedule.setCalculatedAt(LocalDateTime.now());
        
        return schedule;
    }

    /**
     * Update Mother appointments with scheduled times (preserve originals)
     */
    private void updateMotherScheduledTimes(List<CalculateVisitOrderResponseDTO.VisitOrder> visitOrder, FieldVisit fieldVisit) {
        logger.info("Updating individual mother appointments with scheduled times");
        int updatedCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < visitOrder.size(); i++) {
            CalculateVisitOrderResponseDTO.VisitOrder order = visitOrder.get(i);
            logger.debug("Processing mother {} ({}) - position {} in route", 
                        order.getMotherId(), order.getMotherName(), i + 1);
            
            Mother mother = motherRepository.findById(order.getMotherId()).orElse(null);
            
            if (mother != null && mother.getFieldVisitAppointment() != null && 
                fieldVisit.getId().equals(mother.getFieldVisitAppointment().getVisitId())) {
                
                Mother.FieldVisitAppointment appointment = mother.getFieldVisitAppointment();
                String oldStatus = appointment.getStatus();
                String oldScheduledStart = appointment.getScheduledStartTime();
                String oldScheduledEnd = appointment.getScheduledEndTime();
                
                // Set scheduled times (calculated by optimization) - PRESERVE ORIGINAL TIMES
                appointment.setScheduledStartTime(order.getEstimatedArrivalTime());
                appointment.setScheduledEndTime(addMinutesToTime(order.getEstimatedArrivalTime(), order.getEstimatedDuration()));
                appointment.setStatus("ordered");
                
                // Original startTime and endTime remain unchanged!
                
                motherRepository.save(mother);
                updatedCount++;
                
                logger.info("Updated Mother {} appointment:", mother.getName());
                logger.info("  Status: {} -> {}", oldStatus, appointment.getStatus());
                logger.info("  Original times preserved: {} - {}", appointment.getStartTime(), appointment.getEndTime());
                logger.info("  Scheduled times: {} -> {} | {} -> {}", 
                           oldScheduledStart, appointment.getScheduledStartTime(),
                           oldScheduledEnd, appointment.getScheduledEndTime());
                logger.info("  Position in route: {}", i + 1);
            } else {
                errorCount++;
                logger.error("Failed to update Mother {}: mother={}, appointment={}, visitId match={}", 
                            order.getMotherId(), 
                            mother != null ? "found" : "not found",
                            mother != null && mother.getFieldVisitAppointment() != null ? "exists" : "missing",
                            mother != null && mother.getFieldVisitAppointment() != null ? 
                                fieldVisit.getId().equals(mother.getFieldVisitAppointment().getVisitId()) : "N/A");
            }
        }
        
        logger.info("Scheduled time updates completed: {} successful, {} errors", updatedCount, errorCount);
    }

    /**
     * Convert optimized mother list to visit order response format with real navigation data
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
            order.setLatitude(mother.getLatitude());
            order.setLongitude(mother.getLongitude());
            
            // Calculate real travel time and distance from previous location
            if (i == 0) {
                order.setDistance(0.0); // Starting point
                order.setTravelTimeMinutes(0); // No travel for first location
            } else {
                Mother prevMother = optimizedMothers.get(i - 1);
                try {
                    // Create navigation locations
                    NavigationLocation from = NavigationLocation.builder()
                        .latitude(prevMother.getLatitude())
                        .longitude(prevMother.getLongitude())
                        .name(prevMother.getName())
                        .build();
                    
                    NavigationLocation to = NavigationLocation.builder()
                        .latitude(mother.getLatitude())
                        .longitude(mother.getLongitude())
                        .name(mother.getName())
                        .build();
                    
                    // Get real travel time from Google Maps
                    TravelTimeResult travelResult = googleMapsDistanceService.getTravelTime(from, to);
                    
                    if (travelResult.isValid()) {
                        order.setDistance(travelResult.getDistanceMeters());
                        order.setTravelTimeMinutes((int) Math.ceil(travelResult.getDurationInTrafficSeconds() / 60.0));
                        
                        logger.debug("Real navigation data - From {} to {}: distance={}m, time={}min", 
                                   prevMother.getName(), mother.getName(),
                                   travelResult.getDistanceMeters(), 
                                   travelResult.getDurationInTrafficSeconds() / 60);
                    } else {
                        // Fallback to Haversine calculation
                        double distance = calculateHaversineDistance(
                            prevMother.getLatitude(), prevMother.getLongitude(),
                            mother.getLatitude(), mother.getLongitude()
                        );
                        order.setDistance(distance);
                        // Estimate travel time: assume 25 km/h average speed
                        int estimatedMinutes = (int) Math.ceil(distance / (25.0 * 1000 / 60));
                        order.setTravelTimeMinutes(estimatedMinutes);
                        
                        logger.debug("Fallback navigation data - From {} to {}: distance={}m, estimated time={}min", 
                                   prevMother.getName(), mother.getName(), distance, estimatedMinutes);
                    }
                    
                } catch (Exception e) {
                    logger.error("Error getting navigation data from {} to {}, using fallback", 
                               prevMother.getName(), mother.getName(), e);
                    
                    // Complete fallback
                    double distance = calculateHaversineDistance(
                        prevMother.getLatitude(), prevMother.getLongitude(),
                        mother.getLatitude(), mother.getLongitude()
                    );
                    order.setDistance(distance);
                    order.setTravelTimeMinutes((int) Math.ceil(distance / (25.0 * 1000 / 60)));
                }
            }
            
            visitOrder.add(order);
            
            // Calculate next arrival time: current time + travel time + service time
            int totalMinutesToAdd = (order.getTravelTimeMinutes() != null ? order.getTravelTimeMinutes() : 0) + 
                                   order.getEstimatedDuration();
            currentTime = addMinutesToTime(currentTime, totalMinutesToAdd);
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

    /**
     * Convert RouteSchedule to RouteScheduleInfo DTO
     */
    private FieldVisitResponseDTO.RouteScheduleInfo convertToRouteScheduleInfo(FieldVisit.RouteSchedule schedule) {
        FieldVisitResponseDTO.RouteScheduleInfo info = new FieldVisitResponseDTO.RouteScheduleInfo();
        info.setCalculatedAt(schedule.getCalculatedAt());

        // Convert scheduled visits
        if (schedule.getScheduledVisits() != null) {
            info.setScheduledVisits(schedule.getScheduledVisits().stream()
                .map(this::convertToScheduledVisitInfo)
                .collect(Collectors.toList()));
        }

        // Convert metadata
        if (schedule.getMetadata() != null) {
            FieldVisitResponseDTO.RouteScheduleInfo.ScheduleMetadata metadata = 
                new FieldVisitResponseDTO.RouteScheduleInfo.ScheduleMetadata();
            metadata.setTotalDistance(schedule.getMetadata().getTotalDistance());
            metadata.setTotalTravelTime(schedule.getMetadata().getTotalTravelTime());
            metadata.setTotalServiceTime(schedule.getMetadata().getTotalServiceTime());
            metadata.setFellbackToSimple(schedule.getMetadata().getFellbackToSimple());
            info.setMetadata(metadata);
        }

        return info;
    }

    /**
     * Convert ScheduledVisit to ScheduledVisitInfo DTO
     */
    private FieldVisitResponseDTO.RouteScheduleInfo.ScheduledVisitInfo convertToScheduledVisitInfo(
            FieldVisit.RouteSchedule.ScheduledVisit visit) {
        
        FieldVisitResponseDTO.RouteScheduleInfo.ScheduledVisitInfo info = 
            new FieldVisitResponseDTO.RouteScheduleInfo.ScheduledVisitInfo();
        
        info.setMotherId(visit.getMotherId());
        info.setMotherName(visit.getMotherName());
        info.setVisitOrder(visit.getVisitOrder());
        info.setScheduledStartTime(visit.getScheduledStartTime());
        info.setScheduledEndTime(visit.getScheduledEndTime());
        info.setOriginalStartTime(visit.getOriginalStartTime());
        info.setOriginalEndTime(visit.getOriginalEndTime());
        info.setEstimatedDuration(visit.getEstimatedDuration());
        info.setDistanceFromPrevious(visit.getDistanceFromPrevious());
        info.setTravelTimeFromPrevious(visit.getTravelTimeFromPrevious());

        // Convert coordinates
        if (visit.getCoordinates() != null) {
            FieldVisitResponseDTO.RouteScheduleInfo.ScheduledVisitInfo.LocationInfo coords = 
                new FieldVisitResponseDTO.RouteScheduleInfo.ScheduledVisitInfo.LocationInfo();
            coords.setLatitude(visit.getCoordinates().getLatitude());
            coords.setLongitude(visit.getCoordinates().getLongitude());
            coords.setAddress(visit.getCoordinates().getAddress());
            info.setCoordinates(coords);
        }

        return info;
    }
}
