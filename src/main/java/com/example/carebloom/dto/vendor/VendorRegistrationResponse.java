package com.example.carebloom.dto.vendor;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VendorRegistrationResponse {
    private String id;
    private String email;
    private String businessName;
    private String businessRegistrationNumber;
    private String contactNumber;
    private String businessType;
    private List<String> categories;
    private String state;
    private LocalDateTime createdAt;
    private String message;
}
