package com.example.carebloom.services.midwife;

import com.example.carebloom.models.Mother;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class RouteOptimizationService {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

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
     * Optimizes the visit order using Google OR-Tools VRPTW algorithm
     */
    public List<Mother> optimizeVisitOrder(List<Mother> mothers, LocalTime startTime, LocalTime endTime) {
        if (mothers.isEmpty()) {
            log.warn("No mothers provided for route optimization");
            return new ArrayList<>();
        }

        if (mothers.size() == 1) {
            log.info("Only one mother, no optimization needed");
            return mothers;
        }

        // Check if all mothers have valid coordinates
        List<Mother> mothersWithCoordinates = mothers.stream()
            .filter(this::hasValidCoordinates)
            .toList();

        if (mothersWithCoordinates.size() != mothers.size()) {
            log.warn("Some mothers don't have valid coordinates, falling back to simple ordering");
            return fallbackSimpleOrdering(mothers);
        }

        if (!isOrToolsAvailable) {
            log.info("OR-Tools not available, using fallback algorithm");
            return fallbackSimpleOrdering(mothers);
        }

        try {
            // Create data model
            DataModel data = createDataModel(mothersWithCoordinates, startTime, endTime);
            
            // Create routing index manager
            RoutingIndexManager manager = new RoutingIndexManager(
                data.distanceMatrix.length, 
                data.vehicleNumber, 
                data.depot
            );

            // Create routing model
            RoutingModel routing = new RoutingModel(manager);

            // Create and register a transit callback
            final int transitCallbackIndex = routing.registerTransitCallback(
                (long fromIndex, long toIndex) -> {
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    return data.distanceMatrix[fromNode][toNode];
                }
            );

            // Define cost of each arc
            routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

            // Add time window constraints
            addTimeWindowConstraints(routing, manager, data);

            // Set search parameters
            RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .setTimeLimit(com.google.protobuf.Duration.newBuilder().setSeconds(30).build())
                .build();

            // Solve the problem
            Assignment solution = routing.solveWithParameters(searchParameters);

            if (solution != null) {
                return extractOptimizedOrder(solution, manager, routing, mothersWithCoordinates);
            } else {
                log.warn("No solution found for route optimization, falling back to simple ordering");
                return fallbackSimpleOrdering(mothers);
            }

        } catch (Exception e) {
            log.error("Error during route optimization", e);
            return fallbackSimpleOrdering(mothers);
        }
    }

    /**
     * Creates the data model for the VRP solver
     */
    private DataModel createDataModel(List<Mother> mothers, LocalTime startTime, LocalTime endTime) {
        DataModel data = new DataModel();
        
        // Add depot (midwife starting location) at index 0
        // For now, use a central location or the first mother's location
        List<Mother> allLocations = new ArrayList<>();
        allLocations.add(mothers.get(0)); // Depot
        allLocations.addAll(mothers);

        // Create distance matrix
        data.distanceMatrix = calculateDistanceMatrix(allLocations);
        
        // Create time windows
        data.timeWindows = createTimeWindows(mothers, startTime, endTime);
        
        // Set vehicle number and depot
        data.vehicleNumber = 1;
        data.depot = 0;

        return data;
    }

    /**
     * Calculates distance matrix between all locations
     */
    private long[][] calculateDistanceMatrix(List<Mother> locations) {
        int size = locations.size();
        long[][] matrix = new long[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    // Calculate Haversine distance in meters
                    double distance = calculateHaversineDistance(
                        locations.get(i).getLatitude(),
                        locations.get(i).getLongitude(),
                        locations.get(j).getLatitude(),
                        locations.get(j).getLongitude()
                    );
                    matrix[i][j] = Math.round(distance);
                }
            }
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
        
        final int transitCallbackIndex = routing.registerTransitCallback(
            (long fromIndex, long toIndex) -> {
                int fromNode = manager.indexToNode(fromIndex);
                int toNode = manager.indexToNode(toIndex);
                // Assume 30 minutes per visit + travel time based on distance
                long travelTime = data.distanceMatrix[fromNode][toNode] / 500; // Convert distance to time estimate
                return fromNode == toNode ? 0 : travelTime + 30; // 30 minutes service time
            }
        );

        routing.addDimension(
            transitCallbackIndex,
            30, // Allow waiting time of 30 minutes
            24 * 60, // Maximum time per vehicle (24 hours in minutes)
            false, // Don't force start cumul to zero
            timeDimension
        );

        RoutingDimension timeDimensionObject = routing.getMutableDimension(timeDimension);
        
        // Add time window constraints for each location
        for (int i = 0; i < data.timeWindows.length; i++) {
            long index = manager.nodeToIndex(i);
            timeDimensionObject.cumulVar(index).setRange(data.timeWindows[i][0], data.timeWindows[i][1]);
        }

        // Add time window constraints for start of routes
        for (int i = 0; i < data.vehicleNumber; i++) {
            long index = routing.start(i);
            timeDimensionObject.cumulVar(index).setRange(
                data.timeWindows[data.depot][0],
                data.timeWindows[data.depot][1]
            );
        }
    }

    /**
     * Extracts the optimized order from the solution
     */
    private List<Mother> extractOptimizedOrder(Assignment solution, RoutingIndexManager manager, RoutingModel routing, List<Mother> mothers) {
        List<Mother> optimizedOrder = new ArrayList<>();
        
        long index = routing.start(0);
        
        while (!routing.isEnd(index)) {
            int nodeIndex = manager.indexToNode(index);
            if (nodeIndex > 0) { // Skip depot (index 0)
                optimizedOrder.add(mothers.get(nodeIndex - 1));
            }
            index = solution.value(routing.nextVar(index));
        }

        log.info("Route optimization completed. Optimized order: {}", 
            optimizedOrder.stream()
                .map(m -> m.getName() + " (" + m.getId() + ")")
                .toList());

        return optimizedOrder;
    }

    /**
     * Fallback method when optimization fails
     */
    private List<Mother> fallbackSimpleOrdering(List<Mother> mothers) {
        log.info("Using fallback simple time-based ordering");
        
        return mothers.stream()
            .sorted((m1, m2) -> {
                String time1 = getAppointmentTime(m1);
                String time2 = getAppointmentTime(m2);
                return time1.compareTo(time2);
            })
            .toList();
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
