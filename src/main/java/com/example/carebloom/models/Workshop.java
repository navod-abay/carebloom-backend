package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "workshops")
public class Workshop {
    @Id
    private String id;

    private String userId; // ID of the user who created this workshop
    private String mohOfficeId; // ID of the MOH office associated with this workshop

    private String title;
    private String date; // YYYY-MM-DD format
    private String time; // HH:MM format
    private String venue; // Location/venue name
    private String description; // Workshop description
    private String category; // Workshop category
    private Integer capacity; // Maximum number of participants
    private Integer enrolled = 0; // Current number of enrolled participants
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // List of registered mother IDs assigned to this workshop
    private List<String> registeredMotherIds = new ArrayList<>();
    
    // List of mothers with full details (similar to clinics)
    private List<AddedMother> addedMothers = new ArrayList<>();
    
    // Optional: List of unit IDs to assign this workshop to
    private List<String> unitIds = new ArrayList<>();
}
