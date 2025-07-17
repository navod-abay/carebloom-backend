package com.example.carebloom.dto.midwife;

import lombok.Data;
import java.util.List;

@Data
public class FieldVisitCreateDTO {
    private String date; // ISO date string (YYYY-MM-DD)
    private String startTime; // HH:MM format
    private String endTime; // HH:MM format
    private List<String> selectedMotherIds;
}
