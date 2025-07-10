package com.example.carebloom.dto;

import com.example.carebloom.models.Workshop;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateWorkshopResponse {
    private boolean success;
    private String message;
    private Workshop data;
    private Map<String, String> errors;

    public CreateWorkshopResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CreateWorkshopResponse(boolean success, String message, Workshop data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
