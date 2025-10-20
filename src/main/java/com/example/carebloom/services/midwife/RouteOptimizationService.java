package com.example.carebloom.services.midwife;

import com.example.carebloom.dto.navigation.NavigationLocation;
import com.example.carebloom.dto.navigation.TravelTimeResult;
import com.example.carebloom.models.Mother;
import com.example.carebloom.services.navigation.GoogleMapsDistanceService;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class RouteOptimizationService {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Autowired
    private GoogleMapsDistanceService googleMapsDistanceService;

    private boolean isOrToolsAvailable = false;

    public RouteOptimizationService() {
        // Try to load OR-Tools native libraries
        try {
            Loader.loadNativeLibraries();
            isOrToolsAvailable = true;
            log.info("Google OR-Tools native libraries loaded successfully");
        } catch (Exception e) {
            log.warn("Google OR-Tools not available, using fallback algorithm: {}", e.getMessage());
            isOrToolsAvailable = false;
        }
    }

    /**
     * Optimizes the visit order with metrics tracking
     */
    public OptimizationResult optimizeVisitOrderWithMetrics(List<Mother> mothers, LocalTime startTime, LocalTime endTime) {
        long startMs = System.currentTimeMillis();
        boolean fellbackToSimple = false;
        
        try {
            List<Mother> optimized = optimizeVisitOrder(mothers, startTime, endTime);
            long endMs = System.currentTimeMillis();
            
            return new OptimizationResult(optimized, fellbackToSimple, endMs - startMs);
            
        } catch (Exception e) {
            log.warn("OR-Tools failed, falling back to simple algorithm");
            List<Mother> fallback = fallbackSimpleOrdering(mothers);
            long endMs = System.currentTimeMillis();
            
            return new OptimizationResult(fallback, true, endMs - startMs);
        }
    }

    /**
     * Result object containing optimization results and metadata
     */
    @lombok.Data
    public static class OptimizationResult {
        private List<Mother> optimizedOrder;
        private boolean fellbackToSimple;
        private long optimizationTimeMs;
        
        public OptimizationResult(List<Mother> optimizedOrder, boolean fellbackToSimple, long optimizationTimeMs) {
            this.optimizedOrder = optimizedOrder;
            this.fellbackToSimple = fellbackToSimple;
            this.optimizationTimeMs = optimizationTimeMs;
        }
    }

    /**
     * Optimizes the visit order using Google OR-Tools VRPTW algorithm
     */
    public List<Mother> optimizeVisitOrder(List<Mother> mothers, LocalTime startTime, LocalTime endTime) {
        log.info("=== STARTING ROUTE OPTIMIZATION ===");
        log.info("Input: {} mothers, time window: {} - {}", mothers.size(), startTime, endTime);
        
        if (mothers.isEmpty()) {
            log.warn("No mothers provided for route optimization");
            return new ArrayList<>();
        }

        if (mothers.size() == 1) {
            log.info("Only one mother, no optimization needed. Returning: {}", mothers.get(0).getName());
            return mothers;
        }

        // Log input mothers
        log.info("Input mothers:");
        for (int i = 0; i < mothers.size(); i++) {
            Mother m = mothers.get(i);
            log.info("  {}. {} (lat: {}, lon: {}, appointment: {})", 
                i + 1, m.getName(), m.getLatitude(), m.getLongitude(),
                m.getFieldVisitAppointment() != null ? m.getFieldVisitAppointment().getStartTime() : "N/A");
        }

        // Check if all mothers have valid coordinates
        List<Mother> mothersWithCoordinates = mothers.stream()
            .filter(this::hasValidCoordinates)
            .toList();

        log.info("Coordinate validation: {}/{} mothers have valid coordinates", 
                mothersWithCoordinates.size(), mothers.size());

        if (mothersWithCoordinates.size() != mothers.size()) {
            log.warn("Some mothers don't have valid coordinates, falling back to simple ordering");
            log.warn("Mothers with invalid coordinates:");
            mothers.stream()
                .filter(m -> !hasValidCoordinates(m))
                .forEach(m -> log.warn("  - {}: lat={}, lon={}", m.getName(), m.getLatitude(), m.getLongitude()));
            return fallbackSimpleOrdering(mothers);
        }

        if (!isOrToolsAvailable) {
            log.warn("OR-Tools not available, using fallback algorithm");
            return fallbackSimpleOrdering(mothers);
        }

        try {
            log.info("--- STEP 1: Creating data model ---");
            // Create data model
            DataModel data = createDataModel(mothersWithCoordinates, startTime, endTime);
            log.info("Data model created: {}x{} distance matrix, {} vehicles, depot at index {}", 
                    data.distanceMatrix.length, data.distanceMatrix[0].length, 
                    data.vehicleNumber, data.depot);
            
            log.info("--- STEP 2: Creating routing model ---");
            // Create routing index manager
            RoutingIndexManager manager = new RoutingIndexManager(
                data.distanceMatrix.length, 
                data.vehicleNumber, 
                data.depot
            );
            log.info("Routing index manager created for {} nodes", data.distanceMatrix.length);

            // Create routing model
            RoutingModel routing = new RoutingModel(manager);
            log.info("Routing model created successfully");

            log.info("--- STEP 3: Registering transit callback ---");
            // Create and register a transit callback
            final int transitCallbackIndex = routing.registerTransitCallback(
                (long fromIndex, long toIndex) -> {
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    long travelTime = data.distanceMatrix[fromNode][toNode];
                    log.debug("Transit callback: from node {} to node {} = {} seconds", 
                             fromNode, toNode, travelTime);
                    return travelTime;
                }
            );
            log.info("Transit callback registered with index: {}", transitCallbackIndex);

            // Define cost of each arc
            routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);
            log.info("Arc cost evaluator set for all vehicles");

            log.info("--- STEP 4: Adding time window constraints ---");
            // Add time window constraints
            addTimeWindowConstraints(routing, manager, data);
            log.info("Time window constraints added successfully");

            log.info("--- STEP 5: Setting search parameters ---");
            // Set search parameters
            RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .setTimeLimit(com.google.protobuf.Duration.newBuilder().setSeconds(30).build())
                .build();
            log.info("Search parameters: FirstSolution=PATH_CHEAPEST_ARC, LocalSearch=GUIDED_LOCAL_SEARCH, TimeLimit=30s");

            log.info("--- STEP 6: Solving the optimization problem ---");
            long solveStartTime = System.currentTimeMillis();
            // Solve the problem
            Assignment solution = routing.solveWithParameters(searchParameters);
            long solveEndTime = System.currentTimeMillis();
            log.info("Solving completed in {}ms", solveEndTime - solveStartTime);

            if (solution != null) {
                log.info("--- STEP 7: Solution found, extracting optimized order ---");
                log.info("Solution status: {}", routing.status());
                log.info("Solution objective value: {}", solution.objectiveValue());
                return extractOptimizedOrder(solution, manager, routing, mothersWithCoordinates);
            } else {
                log.warn("No solution found for route optimization (solution is null)");
                log.warn("Routing status: {}", routing.status());
                log.warn("Falling back to simple ordering");
                return fallbackSimpleOrdering(mothers);
            }

        } catch (Exception e) {
            log.error("ERROR during route optimization: {}", e.getMessage(), e);
            log.error("Stack trace: ", e);
            log.warn("Falling back to simple ordering due to error");
            return fallbackSimpleOrdering(mothers);
        }
    }

    /**
     * Creates the data model for the VRP solver
     */
    private DataModel createDataModel(List<Mother> mothers, LocalTime startTime, LocalTime endTime) {
        log.info("Creating data model for {} mothers", mothers.size());
        DataModel data = new DataModel();
        
        // Add depot (midwife starting location) at index 0
        // For now, use a central location or the first mother's location
        List<Mother> allLocations = new ArrayList<>();
        allLocations.add(mothers.get(0)); // Depot
        allLocations.addAll(mothers);
        log.info("Total locations (including depot): {}", allLocations.size());
        log.info("Depot location: {} (lat: {}, lon: {})", 
                allLocations.get(0).getName(), 
                allLocations.get(0).getLatitude(), 
                allLocations.get(0).getLongitude());

        // Create distance matrix
        log.info("Calculating distance matrix...");
        data.distanceMatrix = calculateDistanceMatrix(allLocations);
        log.info("Distance matrix calculation completed");
        
        // Create time windows
        log.info("Creating time windows...");
        data.timeWindows = createTimeWindows(mothers, startTime, endTime);
        log.info("Time windows created for {} locations", data.timeWindows.length);
        
        // Log time windows
        log.info("Time window details:");
        log.info("  Depot (index 0): [{}, {}] minutes", data.timeWindows[0][0], data.timeWindows[0][1]);
        for (int i = 1; i < data.timeWindows.length; i++) {
            log.info("  Mother {} (index {}): [{}, {}] minutes", 
                    mothers.get(i-1).getName(), i, data.timeWindows[i][0], data.timeWindows[i][1]);
        }
        
        // Set vehicle number and depot
        data.vehicleNumber = 1;
        data.depot = 0;
        log.info("Data model configuration: {} vehicle(s), depot at index {}", data.vehicleNumber, data.depot);

        return data;
    }

    /**
     * Calculates distance matrix between all locations using Google Maps API
     */
    private long[][] calculateDistanceMatrix(List<Mother> locations) {
        int size = locations.size();
        log.info("Calculating {}x{} distance matrix using Google Maps API", size, size);
        long[][] matrix = new long[size][size];

        try {
            // Convert Mother objects to NavigationLocation objects
            List<NavigationLocation> navLocations = new ArrayList<>();
            for (int i = 0; i < locations.size(); i++) {
                Mother mother = locations.get(i);
                NavigationLocation navLoc = NavigationLocation.builder()
                    .latitude(mother.getLatitude())
                    .longitude(mother.getLongitude())
                    .name(mother.getName())
                    .address(mother.getLocationAddress() != null ? mother.getLocationAddress() : mother.getAddress())
                    .build();
                navLocations.add(navLoc);
                log.debug("Navigation location {}: {} at ({}, {})", 
                         i, navLoc.getName(), navLoc.getLatitude(), navLoc.getLongitude());
            }

            log.info("Calling Google Maps Distance Matrix API for {} locations", navLocations.size());
            // Get travel time matrix from Google Maps
            TravelTimeResult[][] travelMatrix = googleMapsDistanceService.getTravelTimeMatrix(navLocations);
            log.info("Google Maps API call completed");
            
            // Convert travel times to matrix format (in seconds for OR-Tools)
            int validResults = 0;
            int fallbackResults = 0;
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == j) {
                        matrix[i][j] = 0;
                    } else if (travelMatrix[i][j] != null && travelMatrix[i][j].isValid()) {
                        // Use duration in traffic if available, otherwise use regular duration
                        long travelTimeSeconds = travelMatrix[i][j].getDurationInTrafficSeconds();
                        matrix[i][j] = travelTimeSeconds;
                        validResults++;
                        
                        log.debug("Google Maps result from {} to {}: {} seconds ({} minutes)", 
                                 locations.get(i).getName(), locations.get(j).getName(),
                                 travelTimeSeconds, travelTimeSeconds / 60);
                    } else {
                        // Fallback to Haversine distance calculation
                        double distance = calculateHaversineDistance(
                            locations.get(i).getLatitude(),
                            locations.get(i).getLongitude(),
                            locations.get(j).getLatitude(),
                            locations.get(j).getLongitude()
                        );
                        // Convert distance to estimated travel time (assume 25 km/h average speed)
                        long estimatedSeconds = Math.round(distance / (25.0 * 1000 / 3600));
                        matrix[i][j] = estimatedSeconds;
                        fallbackResults++;
                        
                        log.debug("Haversine fallback from {} to {}: {} meters, {} seconds", 
                                 locations.get(i).getName(), locations.get(j).getName(),
                                 distance, estimatedSeconds);
                    }
                }
            }

            log.info("Distance matrix results: {} Google Maps results, {} Haversine fallbacks", 
                    validResults, fallbackResults);
            
            // Log sample of the distance matrix for debugging
            log.debug("Distance matrix sample (first 3x3, in seconds):");
            int logSize = Math.min(3, size);
            for (int i = 0; i < logSize; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < logSize; j++) {
                    row.append(String.format("%6d ", matrix[i][j]));
                }
                log.debug("  Row {}: {}", i, row.toString());
            }
            
        } catch (Exception e) {
            log.error("ERROR calculating distance matrix with Google Maps API: {}", e.getMessage(), e);
            log.warn("Falling back to complete Haversine calculation");
            
            // Complete fallback to Haversine calculation
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == j) {
                        matrix[i][j] = 0;
                    } else {
                        double distance = calculateHaversineDistance(
                            locations.get(i).getLatitude(),
                            locations.get(i).getLongitude(),
                            locations.get(j).getLatitude(),
                            locations.get(j).getLongitude()
                        );
                        // Convert to travel time estimate
                        long estimatedSeconds = Math.round(distance / (25.0 * 1000 / 3600));
                        matrix[i][j] = estimatedSeconds;
                        
                        if (i < 3 && j < 3) { // Log first few for debugging
                            log.debug("Fallback Haversine from {} to {}: {} meters, {} seconds", 
                                     locations.get(i).getName(), locations.get(j).getName(),
                                     distance, estimatedSeconds);
                        }
                    }
                }
            }
            log.info("Complete Haversine fallback completed for {}x{} matrix", size, size);
        }

        return matrix;
    }

    /**
     * Calculates Haversine distance between two coordinates
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
     * Creates time windows for each location
     */
    private long[][] createTimeWindows(List<Mother> mothers, LocalTime startTime, LocalTime endTime) {
        int size = mothers.size() + 1; // +1 for depot
        long[][] timeWindows = new long[size][2];

        // Convert LocalTime to minutes since midnight
        long startMinutes = startTime.getHour() * 60 + startTime.getMinute();
        long endMinutes = endTime.getHour() * 60 + endTime.getMinute();

        // Depot time window
        timeWindows[0][0] = startMinutes;
        timeWindows[0][1] = endMinutes;

        // Mother time windows
        for (int i = 0; i < mothers.size(); i++) {
            Mother mother = mothers.get(i);
            if (mother.getFieldVisitAppointment() != null && 
                mother.getFieldVisitAppointment().getStartTime() != null &&
                mother.getFieldVisitAppointment().getEndTime() != null) {
                
                LocalTime appointmentStart = LocalTime.parse(mother.getFieldVisitAppointment().getStartTime());
                LocalTime appointmentEnd = LocalTime.parse(mother.getFieldVisitAppointment().getEndTime());
                
                timeWindows[i + 1][0] = appointmentStart.getHour() * 60 + appointmentStart.getMinute();
                timeWindows[i + 1][1] = appointmentEnd.getHour() * 60 + appointmentEnd.getMinute();
            } else {
                // Default time window if no appointment time specified
                timeWindows[i + 1][0] = startMinutes;
                timeWindows[i + 1][1] = endMinutes;
            }
        }

        return timeWindows;
    }

    /**
     * Adds time window constraints to the routing model
     */
    private void addTimeWindowConstraints(RoutingModel routing, RoutingIndexManager manager, DataModel data) {
        String timeDimension = "Time";
        log.info("Adding time window constraints with dimension: '{}'", timeDimension);
        
        final int transitCallbackIndex = routing.registerTransitCallback(
            (long fromIndex, long toIndex) -> {
                int fromNode = manager.indexToNode(fromIndex);
                int toNode = manager.indexToNode(toIndex);
                if (fromNode == toNode) {
                    return 0;
                }
                // Use the real travel time from distance matrix (already in seconds)
                // Add 30 minutes (1800 seconds) service time at each location
                long transitTime = data.distanceMatrix[fromNode][toNode] + 1800; // 30 minutes service time
                log.debug("Time constraint transit: from {} to {} = {} seconds (travel: {}, service: 1800)", 
                         fromNode, toNode, transitTime, data.distanceMatrix[fromNode][toNode]);
                return transitTime;
            }
        );
        log.info("Time dimension transit callback registered with index: {}", transitCallbackIndex);

        routing.addDimension(
            transitCallbackIndex,
            1800, // Allow waiting time of 30 minutes (1800 seconds)
            24 * 3600, // Maximum time per vehicle (24 hours in seconds)
            false, // Don't force start cumul to zero
            timeDimension
        );
        log.info("Time dimension added: waiting_time=1800s, max_time_per_vehicle=86400s");

        RoutingDimension timeDimensionObject = routing.getMutableDimension(timeDimension);
        
        // Add time window constraints for each location
        log.info("Setting time window constraints for {} locations", data.timeWindows.length);
        for (int i = 0; i < data.timeWindows.length; i++) {
            long index = manager.nodeToIndex(i);
            // Convert minutes to seconds for time windows
            long startSeconds = data.timeWindows[i][0] * 60;
            long endSeconds = data.timeWindows[i][1] * 60;
            timeDimensionObject.cumulVar(index).setRange(startSeconds, endSeconds);
            
            log.debug("Time window constraint for location {}: [{}, {}] seconds", 
                     i, startSeconds, endSeconds);
        }

        // Add time window constraints for start of routes
        log.info("Setting start route time windows for {} vehicle(s)", data.vehicleNumber);
        for (int i = 0; i < data.vehicleNumber; i++) {
            long index = routing.start(i);
            long startSeconds = data.timeWindows[data.depot][0] * 60;
            long endSeconds = data.timeWindows[data.depot][1] * 60;
            timeDimensionObject.cumulVar(index).setRange(startSeconds, endSeconds);
            
            log.debug("Vehicle {} start time window: [{}, {}] seconds", 
                     i, startSeconds, endSeconds);
        }
        
        // Prioritize minimizing the total time
        timeDimensionObject.setGlobalSpanCostCoefficient(100);
        log.info("Global span cost coefficient set to 100 to minimize total time");
    }

    /**
     * Extracts the optimized order from the solution
     */
    private List<Mother> extractOptimizedOrder(Assignment solution, RoutingIndexManager manager, RoutingModel routing, List<Mother> mothers) {
        log.info("Extracting optimized order from solution");
        List<Mother> optimizedOrder = new ArrayList<>();
        
        log.info("Solution details:");
        log.info("  Objective value: {}", solution.objectiveValue());
        log.info("  Number of vehicles: {}", routing.vehicles());
        
        long index = routing.start(0);
        log.info("Starting route extraction from vehicle 0, start index: {}", index);
        
        int step = 0;
        while (!routing.isEnd(index)) {
            int nodeIndex = manager.indexToNode(index);
            step++;
            
            if (nodeIndex > 0) { // Skip depot (index 0)
                Mother mother = mothers.get(nodeIndex - 1);
                optimizedOrder.add(mother);
                log.info("  Step {}: Visit {} (node {}) - {}", 
                        step, optimizedOrder.size(), nodeIndex, mother.getName());
            } else {
                log.info("  Step {}: At depot (node {})", step, nodeIndex);
            }
            
            long nextIndex = solution.value(routing.nextVar(index));
            log.debug("    Next index: {}", nextIndex);
            index = nextIndex;
        }
        
        log.info("Route extraction completed. Final route:");
        for (int i = 0; i < optimizedOrder.size(); i++) {
            Mother m = optimizedOrder.get(i);
            log.info("  {}. {} (ID: {})", i + 1, m.getName(), m.getId());
        }

        log.info("=== ROUTE OPTIMIZATION COMPLETED SUCCESSFULLY ===");
        log.info("Original order vs Optimized order:");
        log.info("ORIGINAL:");
        for (int i = 0; i < mothers.size(); i++) {
            log.info("  {}. {}", i + 1, mothers.get(i).getName());
        }
        log.info("OPTIMIZED:");
        for (int i = 0; i < optimizedOrder.size(); i++) {
            log.info("  {}. {}", i + 1, optimizedOrder.get(i).getName());
        }

        return optimizedOrder;
    }

    /**
     * Fallback method when optimization fails
     */
    private List<Mother> fallbackSimpleOrdering(List<Mother> mothers) {
        log.warn("=== USING FALLBACK SIMPLE ORDERING ===");
        log.warn("Reason: OR-Tools not available or optimization failed");
        
        List<Mother> result = mothers.stream()
            .sorted((m1, m2) -> {
                String time1 = getAppointmentTime(m1);
                String time2 = getAppointmentTime(m2);
                log.debug("Comparing {} (time: {}) with {} (time: {})", 
                         m1.getName(), time1, m2.getName(), time2);
                return time1.compareTo(time2);
            })
            .toList();
        
        log.warn("Fallback ordering result:");
        for (int i = 0; i < result.size(); i++) {
            Mother m = result.get(i);
            log.warn("  {}. {} (appointment: {})", i + 1, m.getName(), getAppointmentTime(m));
        }
        
        log.warn("=== FALLBACK ORDERING COMPLETED ===");
        return result;
    }

    private String getAppointmentTime(Mother mother) {
        if (mother.getFieldVisitAppointment() != null && 
            mother.getFieldVisitAppointment().getStartTime() != null) {
            return mother.getFieldVisitAppointment().getStartTime();
        }
        return "09:00"; // Default time
    }

    /**
     * Validates that mothers have valid coordinates
     */
    public boolean hasValidCoordinates(Mother mother) {
        return mother.getLatitude() != null && 
               mother.getLongitude() != null &&
               mother.getLatitude() >= -90 && mother.getLatitude() <= 90 &&
               mother.getLongitude() >= -180 && mother.getLongitude() <= 180;
    }

    /**
     * Data model class for the VRP
     */
    private static class DataModel {
        public long[][] distanceMatrix;
        public long[][] timeWindows;
        public int vehicleNumber;
        public int depot;
    }
}
