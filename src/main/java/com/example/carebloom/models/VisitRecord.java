package com.example.carebloom.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "visit_records")
public class VisitRecord {
    public enum VisitType {
        CLINIC, FIELD_VISIT, OTHER
    }

    @Id
    private String id;

    private String motherId;

    private VisitType visitType;

    private LocalDate visitDate;

    private int gestationalWeek;

    private double weight;

    private String bloodPressure;

    private double glucoseLevel;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
