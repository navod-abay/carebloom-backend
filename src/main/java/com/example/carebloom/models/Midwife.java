package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;

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
    
    @Field("clinic")
    private String clinic;
    
    @Field("specialization")
    private String specialization;
    
    @Field("years_of_experience")
    private Integer yearsOfExperience;
    
    @Field("certifications")
    private List<String> certifications;
    
    @Field("phone")
    private String phone;
    
    @Field("email")
    private String email;
    
    @Field("registration_number")
    private String registrationNumber;
    
    @Field("role")
    private String role = "midwife";
    
    @Field("state")
    private String state = "pending";
    
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Field("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Field("created_by")
    private String createdBy;

    // Default constructor
    public Midwife() {}

    // Getters
    public String getId() { return id; }
    public String getOfficeId() { return officeId; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getName() { return name; }
    public String getClinic() { return clinic; }
    public String getSpecialization() { return specialization; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public List<String> getCertifications() { return certifications; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getRole() { return role; }
    public String getState() { return state; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setOfficeId(String officeId) { this.officeId = officeId; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    public void setName(String name) { this.name = name; }
    public void setClinic(String clinic) { this.clinic = clinic; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public void setRole(String role) { this.role = role; }
    public void setState(String state) { this.state = state; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
}
