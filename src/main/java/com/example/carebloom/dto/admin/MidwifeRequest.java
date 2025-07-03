package com.example.carebloom.dto.admin;

import lombok.Data;
import java.util.List;

@Data
public class MidwifeRequest {

    private String officeId;
    
 
    private String name;
    private String clinic;
    private String specialization;
    private Integer yearsOfExperience;
    private List<String> certifications;
    private String phone;
    private String email;
    

    private String registrationNumber;
    private String state; 
}
