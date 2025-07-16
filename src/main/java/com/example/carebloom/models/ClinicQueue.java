package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "clinic_queues")
public class ClinicQueue {
    @Id
    private String id;
    
    private String clinicId;
    private String mohOfficeId;
    private String status = "active"; // active, closed
    private int maxCapacity = 50;
    private int avgAppointmentTime = 15; // in minutes
    private int completedAppointments = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    
    // Queue entries will be stored separately in QueueEntry collection
    // This keeps the queue document lightweight
}
