package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "vendors")
public class Vendor {
    @Id
    private String id;
    private String firebaseUid;
    private String registrationStatus;
    private String email;
    private String name;
    private String businessName;
    private String businessRegistrationNumber;
    private String address;
    private String contactNumber;
    private String role = "vendor";
}
