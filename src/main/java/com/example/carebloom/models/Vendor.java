package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
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
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    private String role = "vendor";
}
