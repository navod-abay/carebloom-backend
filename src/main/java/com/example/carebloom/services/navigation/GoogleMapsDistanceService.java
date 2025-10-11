package com.example.carebloom.services.navigation;

import com.example.carebloom.dto.navigation.NavigationLocation;
import com.example.carebloom.dto.navigation.TravelTimeResult;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GoogleMapsDistanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsDistanceService.class);
    
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    private GeoApiContext context;
    
    // Simple in-memory cache with location-based keys
    private final ConcurrentMap<String, TravelTimeResult> cache = new ConcurrentHashMap<>();
    private static final Duration CACHE_EXPIRY = Duration.ofHours(1);
    
    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("Google Maps API key is not configured");
            return;
        }
        
        this.context = new GeoApiContext.Builder()
            .apiKey(apiKey)
            .build();
        
        logger.info("Google Maps Distance Service initialized successfully");
    }
    
    @PreDestroy
    public void cleanup() {
        if (context != null) {
            context.shutdown();
        }
    }
    
    /**
     * Get travel time between two locations
     */
    public TravelTimeResult getTravelTime(NavigationLocation from, NavigationLocation to) {
        if (!from.hasValidCoordinates() || !to.hasValidCoordinates()) {
            logger.warn("Invalid coordinates provided: from={}, to={}", from, to);
            return createFallbackResult(from, to);
        }
        
        // Check cache first
        String cacheKey = createCacheKey(from, to);
        TravelTimeResult cached = cache.get(cacheKey);
        if (cached != null && isCacheValid(cached)) {
            logger.debug("Using cached travel time for: {} -> {}", from.getName(), to.getName());
            cached.setFromCache(true);
            return cached;
        }
        
        try {
            // Make API call
            LatLng origin = new LatLng(from.getLatitude(), from.getLongitude());
            LatLng destination = new LatLng(to.getLatitude(), to.getLongitude());
            
            DistanceMatrix matrix = DistanceMatrixApi.newRequest(context)
                .origins(origin)
                .destinations(destination)
                .mode(TravelMode.DRIVING)
                .departureTime(java.time.Instant.now())
                .await();
            
            TravelTimeResult result = parseDistanceMatrixResult(matrix);
            
            // Cache the result
            if (result.isValid()) {
                cache.put(cacheKey, result);
                logger.debug("Cached travel time result for: {} -> {}", from.getName(), to.getName());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting travel time from Google Maps API", e);
            return createFallbackResult(from, to);
        }
    }
    
    /**
     * Get travel time matrix for multiple locations (batch processing)
     */
    public TravelTimeResult[][] getTravelTimeMatrix(List<NavigationLocation> locations) {
        int size = locations.size();
        logger.info("=== GOOGLE MAPS DISTANCE MATRIX API CALL ===");
        logger.info("Requesting travel time matrix for {} locations", size);
        
        // Log all locations
        for (int i = 0; i < locations.size(); i++) {
            NavigationLocation loc = locations.get(i);
            logger.info("  Location {}: {} at ({}, {})", 
                       i, loc.getName(), loc.getLatitude(), loc.getLongitude());
        }
        
        TravelTimeResult[][] matrix = new TravelTimeResult[size][size];
        
        // Initialize diagonal (same location) as zero
        for (int i = 0; i < size; i++) {
            matrix[i][i] = TravelTimeResult.builder()
                .duration(Duration.ZERO)
                .durationInTraffic(Duration.ZERO)
                .distanceMeters(0.0)
                .status("OK")
                .calculatedAt(LocalDateTime.now())
                .fromCache(false)
                .build();
        }
        logger.debug("Initialized diagonal elements (same location) as zero travel time");
        
        try {
            // Check API key availability
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.error("Google Maps API key is not configured - falling back to Haversine calculation");
                throw new RuntimeException("API key not available");
            }
            
            // Convert to LatLng array
            LatLng[] latLngs = locations.stream()
                .map(loc -> new LatLng(loc.getLatitude(), loc.getLongitude()))
                .toArray(LatLng[]::new);
            logger.info("Converted {} locations to LatLng array", latLngs.length);
            
            // Make batch API call
            logger.info("Making Google Maps Distance Matrix API call...");
            long apiStartTime = System.currentTimeMillis();
            
            DistanceMatrix distanceMatrix = DistanceMatrixApi.newRequest(context)
                .origins(latLngs)
                .destinations(latLngs)
                .mode(TravelMode.DRIVING)
                .departureTime(java.time.Instant.now())
                .await();
            
            long apiEndTime = System.currentTimeMillis();
            logger.info("Google Maps API call completed in {}ms", apiEndTime - apiStartTime);
            
            // Parse results
            DistanceMatrixRow[] rows = distanceMatrix.rows;
            logger.info("Processing {} rows from Google Maps response", rows.length);
            
            int validResults = 0;
            int invalidResults = 0;
            
            for (int i = 0; i < rows.length && i < size; i++) {
                DistanceMatrixElement[] elements = rows[i].elements;
                logger.debug("Processing row {} with {} elements", i, elements.length);
                
                for (int j = 0; j < elements.length && j < size; j++) {
                    if (i != j) { // Skip diagonal
                        TravelTimeResult result = parseDistanceMatrixElement(elements[j]);
                        matrix[i][j] = result;
                        
                        if (result.isValid()) {
                            validResults++;
                            logger.debug("Valid result from {} to {}: {} seconds, {} meters", 
                                        locations.get(i).getName(), locations.get(j).getName(),
                                        result.getDurationInTrafficSeconds(), result.getDistanceMeters());
                        } else {
                            invalidResults++;
                            logger.warn("Invalid result from {} to {}: status = {}", 
                                       locations.get(i).getName(), locations.get(j).getName(), 
                                       result.getStatus());
                        }
                        
                        // Cache individual results
                        String cacheKey = createCacheKey(locations.get(i), locations.get(j));
                        if (result.isValid()) {
                            cache.put(cacheKey, result);
                            logger.debug("Cached result for key: {}", cacheKey);
                        }
                    }
                }
            }
            
            logger.info("Google Maps API results: {} valid, {} invalid", validResults, invalidResults);
            
        } catch (Exception e) {
            logger.error("ERROR during Google Maps API call: {}", e.getMessage(), e);
            logger.error("Exception details: ", e);
            logger.warn("Falling back to Haversine calculation for missing entries");
            
            // Fallback to Haversine calculation for missing entries
            int fallbackCount = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i != j && matrix[i][j] == null) {
                        matrix[i][j] = createFallbackResult(locations.get(i), locations.get(j));
                        fallbackCount++;
                        
                        if (fallbackCount <= 5) { // Log first few fallbacks
                            logger.debug("Haversine fallback from {} to {}: {} seconds", 
                                        locations.get(i).getName(), locations.get(j).getName(),
                                        matrix[i][j].getDurationInTrafficSeconds());
                        }
                    }
                }
            }
            logger.warn("Created {} Haversine fallback results", fallbackCount);
        }
        
        // Validate matrix completeness
        int nullCount = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (matrix[i][j] == null) {
                    nullCount++;
                    logger.error("NULL result at matrix[{}][{}] - this should not happen!", i, j);
                }
            }
        }
        
        if (nullCount == 0) {
            logger.info("=== DISTANCE MATRIX COMPLETED SUCCESSFULLY ===");
            logger.info("Matrix size: {}x{}, all entries populated", size, size);
        } else {
            logger.error("=== DISTANCE MATRIX COMPLETED WITH ERRORS ===");
            logger.error("Matrix size: {}x{}, {} null entries found!", size, size, nullCount);
        }
        
        return matrix;
    }
    
    /**
     * Parse single distance matrix result
     */
    private TravelTimeResult parseDistanceMatrixResult(DistanceMatrix matrix) {
        if (matrix.rows.length > 0 && matrix.rows[0].elements.length > 0) {
            return parseDistanceMatrixElement(matrix.rows[0].elements[0]);
        }
        
        return TravelTimeResult.builder()
            .status("FAILED")
            .calculatedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Parse distance matrix element
     */
    private TravelTimeResult parseDistanceMatrixElement(DistanceMatrixElement element) {
        TravelTimeResult.TravelTimeResultBuilder builder = TravelTimeResult.builder()
            .calculatedAt(LocalDateTime.now())
            .status(element.status.name())
            .fromCache(false);
        
        if (element.duration != null) {
            builder.duration(Duration.ofSeconds(element.duration.inSeconds));
        }
        
        if (element.durationInTraffic != null) {
            builder.durationInTraffic(Duration.ofSeconds(element.durationInTraffic.inSeconds));
        }
        
        if (element.distance != null) {
            builder.distanceMeters(element.distance.inMeters);
        }
        
        return builder.build();
    }
    
    /**
     * Create fallback result using Haversine distance
     */
    private TravelTimeResult createFallbackResult(NavigationLocation from, NavigationLocation to) {
        double distance = calculateHaversineDistance(
            from.getLatitude(), from.getLongitude(),
            to.getLatitude(), to.getLongitude()
        );
        
        // Estimate travel time: assume average speed of 25 km/h in city
        double averageSpeedMps = 25.0 * 1000 / 3600; // 25 km/h to m/s
        long estimatedSeconds = Math.round(distance / averageSpeedMps);
        
        return TravelTimeResult.builder()
            .duration(Duration.ofSeconds(estimatedSeconds))
            .durationInTraffic(Duration.ofSeconds(estimatedSeconds))
            .distanceMeters(distance)
            .status("ESTIMATED")
            .calculatedAt(LocalDateTime.now())
            .fromCache(false)
            .build();
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
     * Create cache key for location pair
     */
    private String createCacheKey(NavigationLocation from, NavigationLocation to) {
        return String.format("%.6f,%.6f->%.6f,%.6f", 
            from.getLatitude(), from.getLongitude(),
            to.getLatitude(), to.getLongitude());
    }
    
    /**
     * Check if cached result is still valid
     */
    private boolean isCacheValid(TravelTimeResult cached) {
        return cached.getCalculatedAt() != null &&
               Duration.between(cached.getCalculatedAt(), LocalDateTime.now()).compareTo(CACHE_EXPIRY) < 0;
    }
    
    /**
     * Clear expired cache entries
     */
    public void clearExpiredCache() {
        LocalDateTime cutoff = LocalDateTime.now().minus(CACHE_EXPIRY);
        cache.entrySet().removeIf(entry -> 
            entry.getValue().getCalculatedAt().isBefore(cutoff));
        
        logger.debug("Cleared expired cache entries. Current cache size: {}", cache.size());
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("Cache size: %d entries", cache.size());
    }
}
