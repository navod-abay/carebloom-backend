package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "moh_office_users")
public class MoHOfficeUser {
    @Id
    private String id;
    
    @Field("office_id")
    private String officeId;
    
    @Field("email")
    private String email;
    
    @Field("name")
    private String name;
    
    @Field("firebase_uid")
    private String firebaseUid;
    
    @Field("role")
    private String role = "MOH_OFFICE_USER";
    
    @Field("state")
    private String state = "pending"; // pending, active, revoked
    
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Field("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Field("created_by")
    private String createdBy; // Admin who created this user
}
