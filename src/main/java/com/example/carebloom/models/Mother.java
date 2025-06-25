package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "mothers")
public class Mother {
    @Id
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String firebaseUid;
}
