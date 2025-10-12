package com.example.carebloom.dto.queue;

import lombok.Data;

@Data
public class QueueUserDto {
    private String id;
    private String name;
    private String email;
    private String motherId;
    private String clinicId;
    private int position;
    private String status;
    private String joinedTime;
    private String estimatedTime;
    private int waitTime;
    private String notes;
}
