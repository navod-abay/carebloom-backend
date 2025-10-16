package com.example.carebloom.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "queue_users")
public class QueueUser {
    @Id
    private String id;
    private String name;
    private String email;
    private String motherId;
    private String clinicId;
    private int position;
    private String status; // waiting, in-progress, completed, no-show
    private String joinedTime; // ISO timestamp
    private String estimatedTime; // ISO timestamp or HH:mm
    private int waitTime; // Minutes
    private String notes;
}
