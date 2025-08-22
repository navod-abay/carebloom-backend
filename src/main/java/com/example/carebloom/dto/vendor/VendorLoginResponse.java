package com.example.carebloom.dto.vendor;

import lombok.Data;

@Data
public class VendorLoginResponse {
    private String userId;
    private String role;
    private String email;
    private String name;
    private String businessName;
    private boolean success;
    private String message;
}
