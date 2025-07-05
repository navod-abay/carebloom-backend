package com.example.carebloom.dto;

import com.example.carebloom.models.Clinic;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClinicResponse {
    private boolean success;
    private String message;
    private Clinic data;
    private Map<String, String> errors;

    public CreateClinicResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public CreateClinicResponse(boolean success, String message, Clinic data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
