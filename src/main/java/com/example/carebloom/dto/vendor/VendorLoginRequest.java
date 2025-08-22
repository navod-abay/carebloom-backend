package com.example.carebloom.dto.vendor;

import lombok.Data;

@Data
public class VendorLoginRequest {
    private String email;
    private String password;
    private String firebaseUid;
}
