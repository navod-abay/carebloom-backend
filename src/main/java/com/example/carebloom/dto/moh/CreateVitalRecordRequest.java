package com.example.carebloom.dto.moh;

import lombok.Data;

@Data
public class CreateVitalRecordRequest {
    private int gestationalWeek;
    private double weight;
    private String bloodPressure;
    private double glucoseLevel;
    private String notes;
}
