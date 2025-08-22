package com.example.carebloom.services.vendors;

import com.example.carebloom.dto.vendor.VendorRegistrationRequest;
import com.example.carebloom.models.Vendor;
import com.example.carebloom.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@Service
public class VendorRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(VendorRegistrationService.class);

    @Autowired
    private VendorRepository vendorRepository;

    public Vendor registerVendor(VendorRegistrationRequest request) {
        // Check if vendor already exists by email
        if (vendorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Vendor with email " + request.getEmail() + " already exists");
        }

        // Create new vendor
        Vendor vendor = new Vendor();
        vendor.setEmail(request.getEmail());
        vendor.setBusinessName(request.getBusinessName());
        vendor.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        vendor.setContactNumber(request.getContactNumber());
        vendor.setBusinessType(request.getBusinessType());
        vendor.setCategories(request.getCategories());
        vendor.setState("pending"); // Always set to pending for new registrations
        vendor.setCreatedAt(LocalDateTime.now());
        vendor.setRole("vendor");

        // Save vendor to database
        Vendor savedVendor = vendorRepository.save(vendor);

        logger.info("Vendor registered successfully with ID: {}", savedVendor.getId());
        return savedVendor;
    }
}
