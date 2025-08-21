package com.example.carebloom.services.admin;

import com.example.carebloom.models.Vendor;
import com.example.carebloom.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
public class PlatformAdminVendorService {
    private static final Logger logger = LoggerFactory.getLogger(PlatformAdminVendorService.class);

    @Autowired
    private VendorRepository vendorRepository;

    /**
     * Accept vendor registration - sets state to "approved"
     */
    public void acceptVendorRegistration(String vendorId) {
        Optional<Vendor> vendorOpt = vendorRepository.findById(vendorId);
        if (vendorOpt.isEmpty()) {
            logger.error("Vendor not found with ID: {}", vendorId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found");
        }

        Vendor vendor = vendorOpt.get();
        if (!"pending".equalsIgnoreCase(vendor.getState())) {
            logger.error("Vendor {} state is not 'pending': {}", vendorId, vendor.getState());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vendor state must be 'pending' to accept");
        }

        vendor.setState("approved");
        vendorRepository.save(vendor);
        logger.info("Vendor {} registration accepted", vendorId);
    }

    // TODO: Commented out old implementation - keeping for reference
    // public void acceptVendorRegistration(String vendorId) {
    // Optional<Vendor> vendorOpt = vendorRepository.findById(vendorId);
    // if (vendorOpt.isEmpty()) {
    // logger.error("Vendor not found with ID: {}", vendorId);
    // throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found");
    // }
    // Vendor vendor = vendorOpt.get();
    // if (!"complete".equalsIgnoreCase(vendor.getRegistrationStatus())) {
    // logger.error("Vendor {} registration status is not 'completed': {}",
    // vendorId,
    // vendor.getRegistrationStatus());
    // throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
    // "Vendor registration status must be 'completed' to accept");
    // }
    // vendor.setRegistrationStatus("accepted");
    // vendorRepository.save(vendor);
    // logger.info("Vendor {} registration accepted", vendorId);
    // }
}
