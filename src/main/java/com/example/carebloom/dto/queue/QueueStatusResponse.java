package com.example.carebloom.dto.queue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.carebloom.models.QueueEntry;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private boolean success;
    private String message;
    private QueueData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueData {
        private String id;
        private String clinicId;
        private String status;
        private int maxCapacity;
        private int avgAppointmentTime;
        private int completedAppointments;
        private int currentQueueLength;
        private List<QueueEntry> entries;
        private QueueStatistics statistics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueStatistics {
        private int averageWaitTime;
        private int lastPersonWaitTime;
        private int totalWaitTime;
    }
}
