package com.example.carebloom.dto.vendor;

import lombok.Data;
import java.util.List;

@Data
public class VendorRegistrationRequest {
    private String email;
    private String businessName;
    private String businessRegistrationNumber;
    private String contactNumber;
    private String businessType; // 'online' | 'physical' | 'both'
    private List<String> categories;
}
