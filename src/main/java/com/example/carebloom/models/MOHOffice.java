package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "moh_offices")
public class MOHOffice {
    @Id
    private String id;
    private String firebaseUid;
    private String email;
    private String officeName;
    private String district;
    private String division;
    private String registrationNumber;
    private String contactNumber;
    private String role = "moh-office";
}
