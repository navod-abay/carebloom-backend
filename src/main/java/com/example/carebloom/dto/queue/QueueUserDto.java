package com.example.carebloom.dto.queue;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueUserDto {
    private String name;
    private String email;
    private int waitTime;
    private String joinedTime;
    private int queuePosition;
    private String status;
}
