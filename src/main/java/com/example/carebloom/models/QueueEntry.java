package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "queue_entries")
public class QueueEntry {
    @Id
    private String id;
    
    private String queueId;
    private String clinicId;
    private String name;
    private String email;
    private int queuePosition;
    private String status = "waiting"; // waiting, current, completed
    private LocalDateTime joinedAt;
    private LocalDateTime completedAt;
    private int estimatedWaitTime; // in minutes
}
