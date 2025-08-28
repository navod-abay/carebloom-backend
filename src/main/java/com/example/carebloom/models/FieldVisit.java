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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
