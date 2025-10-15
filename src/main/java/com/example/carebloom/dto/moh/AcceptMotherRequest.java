package com.example.carebloom.dto.moh;

import com.example.carebloom.models.HealthDetails;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AcceptMotherRequest {
    private String unitId;
    private HealthDetailsRequest healthDetails;

    @Data
    public static class HealthDetailsRequest {
        private int age;
        private HealthDetails.BloodType bloodType;
        private String allergies;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private HealthDetails.PregnancyType pregnancyType;
    }
}
