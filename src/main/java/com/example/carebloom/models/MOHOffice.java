package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "moh_offices")
public class MOHOffice {
    @Id
    private String id;
    private String divisionalSecretariat;
    private String district;  // Added district field
    private String address;
    private String officerInCharge;
    private String contactNumber;
    private String adminEmail;
    private String role = "MOH_OFFICE";
}