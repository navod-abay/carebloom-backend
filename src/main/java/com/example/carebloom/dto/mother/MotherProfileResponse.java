package com.example.carebloom.dto.mother;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MotherProfileResponse {
    private String id;
    private String email;
    private String name;
    private String phone;
    private String dueDate;
    private String address;
    private String district;
    private String mohOfficeId;
    private String areaMidwifeId;
    private String recordNumber;
    private String unitId;
    private String profilePhotoUrl;
    private LocationCoordinates location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class LocationCoordinates {
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private Long timestamp;
    }
}