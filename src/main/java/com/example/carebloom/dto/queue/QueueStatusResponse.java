package com.example.carebloom.dto.queue;

import lombok.Data;
import java.util.List;

@Data
public class QueueStatusResponse {
    private String clinicId;
    private boolean isActive;
    private QueuePatientDto currentPatient;
    private List<QueuePatientDto> waitingQueue;
    private QueueStatsDto stats;

    @Data
    public static class QueuePatientDto {
        private String name;
        private String email;
        private int position;
        private String status;
        private Integer estimatedWaitTime;
    }

    @Data
    public static class QueueStatsDto {
        private int totalPatients;
        private int completed;
        private int waiting;
        private int inProgress;
    }
}
