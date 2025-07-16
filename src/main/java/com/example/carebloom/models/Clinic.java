package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Document(collection = "clinics")
public class Clinic {
    @Id
    private String id;
    private String userId; // ID of the user who created this clinic
    private String mohOfficeId; // ID of the user who created this clinic
    private String title;
    private String date; // YYYY-MM-DD format
    private String startTime;
    private String doctorName;
    private String location;
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Optional: List of registered mother IDs assigned to this clinic
    private List<String> registeredMotherIds = new ArrayList<>();
    
    // Optional: Maximum number of mothers that can be assigned to this clinic
    private Integer maxCapacity;
    
    // Optional: Additional notes about the clinic
    private String notes;
}
