package com.example.carebloom.services.vendors;

import com.example.carebloom.models.Vendor;
import com.example.carebloom.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VendorAuthService {

    private static final Logger logger = LoggerFactory.getLogger(VendorAuthService.class);

    @Autowired
    private VendorRepository vendorRepository;

    /**
     * Find vendor by Firebase UID
     */
    public Vendor findByFirebaseUid(String firebaseUid) {
        return vendorRepository.findByFirebaseUid(firebaseUid);
    }

    /**
     * Find vendor by email
     */
    public Vendor findByEmail(String email) {
        return vendorRepository.findByEmail(email);
    }

    /**
     * Save vendor
     */
    public Vendor save(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    /**
     * Check if vendor exists by Firebase UID
     */
    public boolean existsByFirebaseUid(String firebaseUid) {
        return vendorRepository.findByFirebaseUid(firebaseUid) != null;
    }

    /**
     * Check if vendor exists by email
     */
    public boolean existsByEmail(String email) {
        return vendorRepository.findByEmail(email) != null;
    }
}
