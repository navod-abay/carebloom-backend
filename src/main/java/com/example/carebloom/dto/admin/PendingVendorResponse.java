package com.example.carebloom.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingVendorResponse {
    private String id;
    private String firebaseUid;
    private String email;
    private String businessName;
    private String businessRegistrationNumber;
    private String contactNumber;
    private String businessType;
    private List<String> categories;
    private String state;
    private LocalDateTime createdAt;
    private String role;
}
