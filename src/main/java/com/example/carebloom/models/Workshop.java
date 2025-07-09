package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "workshops")
public class Workshop {
    @Id
    private String id;
    
    private String userId;      // ID of the user who created this workshop
    
    private String title;
    private String date;        // YYYY-MM-DD format
    private String time;        // HH:MM format
    private String venue;       // Location/venue name
    private String description; // Workshop description
    private String category;    // Workshop category
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
