package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "field_visits")
public class FieldVisit {
    @Id
    private String id;
    private String midwifeId;
    private String date; // ISO date string (YYYY-MM-DD)
    private String startTime; // HH:MM format
    private String endTime; // HH:MM format
    private List<String> selectedMotherIds;
    private String status; // SCHEDULED, CALCULATED,IN_PROGRESS, COMPLETED, CANCELLED
    
    // NEW: Comprehensive schedule object
    private RouteSchedule schedule;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class RouteSchedule {
        private List<ScheduledVisit> scheduledVisits; // Ordered list
        private ScheduleMetadata metadata;
        private LocalDateTime calculatedAt;
        
        @Data
        public static class ScheduledVisit {
            private String motherId;
            private String motherName;
            private int visitOrder; // 1-based position in route
            private String scheduledStartTime; // HH:MM - calculated optimal time
            private String scheduledEndTime;   // HH:MM - calculated end time
            private String originalStartTime;  // HH:MM - user's preferred time
            private String originalEndTime;    // HH:MM - user's preferred end time
            private Integer estimatedDuration; // minutes
            private Double distanceFromPrevious; // meters
            private Integer travelTimeFromPrevious; // minutes
            private RouteCoordinates coordinates;
            
            @Data
            public static class RouteCoordinates {
                private Double latitude;
                private Double longitude;
                private String address;
            }
        }
        
        @Data
        public static class ScheduleMetadata {
            private Double totalDistance; // meters
            private Integer totalTravelTime; // minutes
            private Integer totalServiceTime; // minutes
            private Boolean fellbackToSimple; // if OR-Tools failed
        }
    }
}
