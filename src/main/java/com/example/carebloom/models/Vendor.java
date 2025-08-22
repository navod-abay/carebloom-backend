package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "vendors")
public class Vendor {
    @Id
    private String id;
    private String firebaseUid;
    private String email;
    private String businessName;
    private String businessRegistrationNumber;
    private String contactNumber;
    private String businessType; // 'online' | 'physical' | 'both'
    private List<String> categories;
    private String state = "pending"; // 'pending' | 'approved' | 'suspended' | 'revoked'
    private LocalDateTime createdAt = LocalDateTime.now();
    private String role = "vendor";
}
