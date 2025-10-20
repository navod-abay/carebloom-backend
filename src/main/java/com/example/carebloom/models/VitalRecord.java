package com.example.carebloom.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "visit_records")
public class VitalRecord {
    public enum VisitType {
        CLINIC, HOME_VISIT, OTHER
    }

    @Id
    private String id;

    @Indexed
    private String motherId;

    @Indexed
    private LocalDate recordedDate;

    private VisitType visitType;

    private int gestationalWeek;

    private double weight;

    private String bloodPressure;

    private double glucoseLevel;

    private Double bodyTemperature;

    private double heartRate;

    private String notes;  // Added field for visit notes

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String midwifeId;

    private String fieldVisitId;
    
    private String clinicId;
}
