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
        A_POSITIVE, A_NEGATIVE,
        B_POSITIVE, B_NEGATIVE,
        AB_POSITIVE, AB_NEGATIVE,
        O_POSITIVE, O_NEGATIVE
    }

    public enum PregnancyType {
        SINGLE, TWIN, MULTIPLE
    }

    @Id
    private String id;

    private String motherId;

    private int age;

    private BloodType bloodType;

    private String allergies;

    private String emergencyContactName;

    private String emergencyContactPhone;

    private PregnancyType pregnancyType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
