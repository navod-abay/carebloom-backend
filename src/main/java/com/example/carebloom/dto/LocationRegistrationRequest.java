package com.example.carebloom.dto;

import lombok.Data;

@Data
public class LocationRegistrationRequest {
    private String district;
    private String mohOfficeId; // Reference to MOHOffice ID
    private String areaMidwifeId; // Reference to Midwife ID
    private String unitId; // Reference to Unit ID
    private String recordNumber;
    private LocationCoordinates location; // GPS coordinates from frontend
    
    @Data
    public static class LocationCoordinates {
        private Double latitude;
        private Double longitude;
        private Double accuracy; // GPS accuracy in meters
        private Long timestamp; // Unix timestamp when location was captured
    }
}
