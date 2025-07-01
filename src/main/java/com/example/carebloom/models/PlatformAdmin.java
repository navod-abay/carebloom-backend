package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "platform_admins")
public class PlatformAdmin {
    @Id
    private String id;
    private String firebaseUid;
    private String email;
    private String name;
    private String role = "PLATFORM_MANAGER";
}
