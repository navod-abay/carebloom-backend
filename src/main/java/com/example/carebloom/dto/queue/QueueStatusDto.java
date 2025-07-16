package com.example.carebloom.dto.queue;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueStatusDto {
    private String id;
    private String clinicId;
    private String status;
    private QueueSettingsDto settings;
    private QueueUserDto currentUser;
    private List<QueueUserDto> waitingUsers;
    private int completedCount;
    private QueueStatisticsDto statistics;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueueSettingsDto {
        private int maxCapacity;
        private int avgAppointmentTime;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueueStatisticsDto {
        private int totalWaitTime;
        private int avgWaitTime;
        private int queueLength;
    }
}
