package com.example.carebloom.models;

import lombok.Data;

@Data
public class UserProfile {
    private String id;
    private String name;
    private String email;
    private String role;
    private String officeId; // MoH office ID for office users
    private String registrationStatus; // 'initial', 'location_pending', 'personal_pending', 'complete'
    private String divisionalSecretariat; // Divisional secretariat from MOH office
    private String district; // District from MOH office
}
