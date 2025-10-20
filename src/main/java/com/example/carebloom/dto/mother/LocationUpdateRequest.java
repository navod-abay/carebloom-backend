package com.example.carebloom.dto.mother;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

@Data
public class LocationUpdateRequest {
    @Valid
    @NotNull(message = "Location data is required")
    private LocationCoordinates location;
    
    @Data
    public static class LocationCoordinates {
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        private Double latitude;
        
        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        private Double longitude;
        
        @Positive(message = "Accuracy must be positive")
        private Double accuracy; // GPS accuracy in meters
        
        @Positive(message = "Timestamp must be positive")
        private Long timestamp; // Unix timestamp when location was captured
    }
}