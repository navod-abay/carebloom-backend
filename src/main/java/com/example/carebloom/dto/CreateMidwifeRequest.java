package com.example.carebloom.dto;

import java.util.List;

public class CreateMidwifeRequest {
    private String name;
    private String clinic;
    private String specialization;
    private Integer yearsOfExperience;
    private List<String> certifications;
    private String phone;
    private String email;
    private String mohOfficeId;

    // Default constructor
    public CreateMidwifeRequest() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClinic() {
        return clinic;
    }

    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMohOfficeId() {
        return mohOfficeId;
    }

    public void setMohOfficeId(String mohOfficeId) {
        this.mohOfficeId = mohOfficeId;
    }

    @Override
    public String toString() {
        return "CreateMidwifeRequest{" +
                "name='" + name + '\'' +
                ", clinic='" + clinic + '\'' +
                ", specialization='" + specialization + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                ", certifications=" + certifications +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", mohOfficeId='" + mohOfficeId + '\'' +
                '}';
    }
}
