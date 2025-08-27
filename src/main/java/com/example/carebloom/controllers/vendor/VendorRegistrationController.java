package com.example.carebloom.controllers.vendor;

import com.example.carebloom.dto.vendor.VendorRegistrationRequest;
import com.example.carebloom.dto.vendor.VendorRegistrationResponse;
import com.example.carebloom.models.Vendor;
import com.example.carebloom.services.vendors.VendorRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/vendor/auth")
@CrossOrigin(origins = "${app.cors.vendor-origin}")
public class VendorRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(VendorRegistrationController.class);

    @Autowired
    private VendorRegistrationService vendorRegistrationService;

    @PostMapping("/vendor-registration")
    public ResponseEntity<?> registerVendor(@RequestBody VendorRegistrationRequest request) {
        try {
            logger.info("Registering vendor with email: {}", request.getEmail());

            Vendor vendor = vendorRegistrationService.registerVendor(request);

            VendorRegistrationResponse response = new VendorRegistrationResponse();
            response.setId(vendor.getId());
            response.setEmail(vendor.getEmail());
            response.setBusinessName(vendor.getBusinessName());
            response.setBusinessRegistrationNumber(vendor.getBusinessRegistrationNumber());
            response.setContactNumber(vendor.getContactNumber());
            response.setBusinessType(vendor.getBusinessType());
            response.setCategories(vendor.getCategories());
            response.setState(vendor.getState());
            response.setCreatedAt(vendor.getCreatedAt());
            response.setMessage("Vendor registration submitted successfully. Your application is pending approval.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error registering vendor: {}", e.getMessage());
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }
}
