package com.example.carebloom.dto.queue;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueResponse {
    private boolean success;
    private String message;
    private Object data;
    
    public QueueResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
