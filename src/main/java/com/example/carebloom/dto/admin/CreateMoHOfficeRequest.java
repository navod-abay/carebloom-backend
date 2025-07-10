package com.example.carebloom.dto.admin;

import lombok.Data;

@Data
public class CreateMoHOfficeRequest {
    private String divisionalSecretariat;
    private String district; // Add district field
    private String address;
    private Location location;
    private String officerInCharge;
    private String contactNumber;
    private String adminEmail;

    @Data
    public static class Location {
        private double latitude;
        private double longitude;
    }
}
