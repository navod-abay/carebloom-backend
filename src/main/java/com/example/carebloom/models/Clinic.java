package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "clinics")
public class Clinic {
    @Id
    private String id;
    private String title;
    private String date; // YYYY-MM-DD format
    private String startTime;
    private String doctorName;
    private String location;
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
