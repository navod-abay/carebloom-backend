package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
@Document(collection = "midwives")
public class Midwife {
    @Id
    private String id;

    @Field("office_id")
    private String officeId;

    @Field("firebase_uid")
    private String firebaseUid;

    @Field("name")
    private String name;

    @Field("phone")
    private String phone;

    @Field("email")
    private String email;

    @Field("state")
    private String state = "pending";

    // Profile fields
    @Field("address")
    private String address;

    @Field("profile_image")
    private String profileImage;

    @Field("specialization")
    private String specialization;

    @Field("experience")
    private Integer experience; // Years of experience

    @Field("license_number")
    private String licenseNumber;

    @Field("working_hours")
    private String workingHours;

    @Field("bio")
    private String bio;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("assigned_mother_ids")
    private List<String> assignedMotherIds = new ArrayList<>();

    @Field("assigned_unit_ids")
    private List<String> assignedUnitIds = new ArrayList<>();

    // Helper methods for assigned mothers
    public void addAssignedMother(String motherId) {
        if (this.assignedMotherIds == null) {
            this.assignedMotherIds = new ArrayList<>();
        }
        this.assignedMotherIds.add(motherId);
    }

    public void removeAssignedMother(String motherId) {
        if (this.assignedMotherIds != null) {
            this.assignedMotherIds.remove(motherId);
        }
    }

    // Helper methods for assigned units
    public void addAssignedUnit(String unitId) {
        if (this.assignedUnitIds == null) {
            this.assignedUnitIds = new ArrayList<>();
        }
        this.assignedUnitIds.add(unitId);
    }

    public void removeAssignedUnit(String unitId) {
        if (this.assignedUnitIds != null) {
            this.assignedUnitIds.remove(unitId);
        }
    }

}
