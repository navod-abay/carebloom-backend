package com.example.carebloom.dto.moh;

import com.example.carebloom.models.HealthDetails;
import lombok.Data;

@Data
public class UpdateHealthDetailsRequest {
    private int age;
    private HealthDetails.BloodType bloodType;
    private String allergies;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private HealthDetails.PregnancyType pregnancyType;
}
