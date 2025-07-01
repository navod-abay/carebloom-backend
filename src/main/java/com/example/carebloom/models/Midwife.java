package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "midwives")
public class Midwife {
    @Id
    private String id;
    private String firebaseUid;
    private String email;
    private String name;
    private String registrationNumber;
    private String district;
    private String division;
    private String role = "midwife";
}
