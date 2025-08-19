package com.example.carebloom.dto.vendor;

import lombok.Data;

@Data
public class VendorRegistrationRequest {
    private String firebaseUid;
    private String email;
    private String name;
    private String businessName;
    private String businessRegistrationNumber;
    private String address;
    private String contactNumber;
}
