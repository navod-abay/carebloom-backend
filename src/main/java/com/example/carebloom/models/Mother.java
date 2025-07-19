package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "mothers")
public class Mother {
    @Id
    private String id;
    private String firebaseUid;
    private String registrationStatus;
    
    // Personal details (Second Step)
    private String name;
    private String email;
    private String phone;
    private String dueDate;
    private String address;
    
    // Location details (Final Step)
    private String district;
    private String mohOfficeId; // Reference to MOHOffice table
    private String areaMidwifeId; // Reference to Midwife table
    private String recordNumber;

    private String UnitId;
}
