package com.example.carebloom.dto.admin;

import lombok.Data;
import java.util.List;

@Data
public class MidwifeRequest {
    // For create operations
    private String officeId;
    
    // Main midwife information
    private String name;
    private String clinic;
    private String specialization;
    private Integer yearsOfExperience;
    private List<String> certifications;
    private String phone;
    private String email;
    
    // Optional fields for updates
    private String registrationNumber;
    private String state; 
}
