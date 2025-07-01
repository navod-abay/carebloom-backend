package com.example.carebloom.models;

import lombok.Data;

@Data
public class UserProfile {
    private String id;
    private String name;
    private String email;
    private String role;
    private String registrationStatus; // 'initial', 'location_pending', 'personal_pending', 'complete'
}
