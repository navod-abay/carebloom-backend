package com.example.carebloom.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateClinicRequest {
    private String title;
    private String date; // YYYY-MM-DD format
    private String startTime;
    private String doctorName;
    private String location;
    private List<String> registeredMotherIds; // Optional: List of mother IDs to assign to this clinic
    private Integer maxCapacity; // Optional: Maximum number of mothers
    private String notes; // Optional: Additional notes
    private List<String> unitIds; // Optional: List of unit IDs to assign this clinic to
}
