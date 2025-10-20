package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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

    // Audit fields for tracking registration dates
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

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

    // Notification fields
    private String fcmToken;
    private LocalDateTime fcmTokenUpdatedAt;
    private NotificationPreferences notificationPreferences;

    // Field visit appointment details
    private FieldVisitAppointment fieldVisitAppointment;

    @Data
    public static class NotificationPreferences {
        private Boolean emergencyAlerts = true; // Always true, cannot be disabled
        private Boolean appUpdates = true;
        private Boolean mohOfficeNotifications = true;
        private Boolean trimesterUpdates = true;
    }

    @Data
    public static class FieldVisitAppointment {
        private String visitId; // Reference to FieldVisit ID
        private String date; // ISO date string (YYYY-MM-DD)
        private String startTime; // HH:MM format - Original preferred time
        private String endTime; // HH:MM format - Original preferred end time
        
        // NEW: Calculated optimal times
        private String scheduledStartTime; // HH:MM - Calculated by optimization
        private String scheduledEndTime;   // HH:MM - Calculated by optimization
        
        private String status; // "new", "confirmed", "ordered", "ongoing", "completed", "rescheduled"
    }
}
