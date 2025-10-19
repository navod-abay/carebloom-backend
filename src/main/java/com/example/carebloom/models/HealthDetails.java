package com.example.carebloom.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "health_details")
public class HealthDetails {
    public enum BloodType {
        A_POSITIVE("A+"),
        A_NEGATIVE("A-"),
        B_POSITIVE("B+"),
        B_NEGATIVE("B-"),
        AB_POSITIVE("AB+"),
        AB_NEGATIVE("AB-"),
        O_POSITIVE("O+"),
        O_NEGATIVE("O-");

        private final String displayValue;

        BloodType(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }
    }

    public enum PregnancyType {
        SINGLE, TWIN, MULTIPLE
    }

    @Id
    private String id;

    private String motherId;

    private Integer age;

    private BloodType bloodType;

    private String allergies;

    private String emergencyContactName;

    private String emergencyContactPhone;

    private PregnancyType pregnancyType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
