package com.example.carebloom.dto.midwife;

import lombok.Data;
import java.util.List;

@Data
public class CalculateVisitOrderResponseDTO {
    private boolean success;
    private String message;
    private List<VisitOrder> visitOrder;
    private Double totalDistance; // total distance in meters
    private Integer totalEstimatedTime; // total time in minutes
    
    @Data
    public static class VisitOrder {
        private String motherId;
        private String motherName;
        private String address;
        private String estimatedArrivalTime; // HH:MM format
        private Integer estimatedDuration; // minutes
        private Double distance; // meters from previous location
    }
}
