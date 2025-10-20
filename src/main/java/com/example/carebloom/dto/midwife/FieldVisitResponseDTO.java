package com.example.carebloom.dto.midwife;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FieldVisitResponseDTO {
    private String id;
    private String date;
    private String startTime;
    private String endTime;
    private String midwifeId;
    private List<MotherBasicInfo> mothers;
    private String status; // 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enhanced route schedule information
    private RouteScheduleInfo schedule;
    
    @Data
    public static class MotherBasicInfo {
        private String id;
        private String name;
        private String startTime;
        private String endTime;
        private String status; // 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
    }

    @Data
    public static class RouteScheduleInfo {
        private LocalDateTime calculatedAt;
        private List<ScheduledVisitInfo> scheduledVisits;
        private ScheduleMetadata metadata;

        @Data
        public static class ScheduledVisitInfo {
            private String motherId;
            private String motherName;
            private Integer visitOrder;
            private String scheduledStartTime;
            private String scheduledEndTime;
            private String originalStartTime;
            private String originalEndTime;
            private Integer estimatedDuration; // in minutes
            private Double distanceFromPrevious; // in kilometers
            private Integer travelTimeFromPrevious; // in minutes
            private LocationInfo coordinates;

            @Data
            public static class LocationInfo {
                private Double latitude;
                private Double longitude;
                private String address;
            }
        }

        @Data
        public static class ScheduleMetadata {
            private Double totalDistance; // in kilometers
            private Integer totalTravelTime; // in minutes
            private Integer totalServiceTime; // in minutes
            private Boolean fellbackToSimple;
        }
    }
}
