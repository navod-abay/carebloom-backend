package com.example.carebloom.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

        @JsonValue
        public String getDisplayValue() {
            return displayValue;
        }
    }

    public enum PregnancyType {
        SINGLE, TWIN, MULTIPLE
    }

    @Id
    @JsonProperty("id")
    private String id;

    @Field("motherId")
    @JsonProperty("motherId")
    private String motherId;

    @Field("age")
    @JsonProperty("age")
    private Integer age;

    @Field("bloodType")
    @JsonProperty("bloodType")
    private BloodType bloodType;

    @Field("allergies")
    @JsonProperty("allergies")
    private String allergies;

    @Field("emergencyContactName")
    @JsonProperty("emergencyContactName")
    private String emergencyContactName;

    @Field("emergencyContactPhone")
    @JsonProperty("emergencyContactPhone")
    private String emergencyContactPhone;

    @Field("pregnancyType")
    @JsonProperty("pregnancyType")
    private PregnancyType pregnancyType;

    @Field("createdAt")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
