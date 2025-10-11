package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Document(collection = "mothers")
public class Mother {
    @Id
    private String id;
    private String firebaseUid;
    private String registrationStatus;
    
    // Personal details (Second Step)
    private String name;
    private String email;
    private String phone;
    private String dueDate;
    private String address;
    
    // Location details (Final Step)
    private String district;
    private String mohOfficeId; // Reference to MOHOffice table
    private String areaMidwifeId; // Reference to Midwife table
    private String recordNumber;
    
    // Geographic coordinates for route optimization
    private Double latitude;
    private Double longitude;
    private String locationAddress; // Human-readable address for verification

    private String unitId;
    
    // Profile photo fields
    private String profilePhotoUrl;
    private LocalDateTime profilePhotoUploadedAt;
    
    // Field visit appointment details
    private FieldVisitAppointment fieldVisitAppointment;
    
    @Data
    public static class FieldVisitAppointment {
        private String visitId; // Reference to FieldVisit ID
        private String date; // ISO date string (YYYY-MM-DD)
        private String startTime; // HH:MM format
        private String endTime; // HH:MM format
        private String status; // "new", "confirmed", "ordered", "ongoing", "completed", "rescheduled"
    }
}
