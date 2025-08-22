package com.example.carebloom.services.admin;

import com.example.carebloom.dto.admin.PendingVendorResponse;
import com.example.carebloom.models.Vendor;
import com.example.carebloom.repositories.VendorRepository;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorManagementService {

    private static final Logger logger = LoggerFactory.getLogger(VendorManagementService.class);

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private FirebaseUserService firebaseUserService;

    /**
     * Get all pending vendors for admin approval
     */
    public List<PendingVendorResponse> getAllPendingVendors() {
        logger.info("Fetching all pending vendors");

        List<Vendor> pendingVendors = vendorRepository.findByState("pending");

        List<PendingVendorResponse> response = pendingVendors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} pending vendors", response.size());
        return response;
    }

    /**
     * Get all vendors regardless of status
     */
    public List<PendingVendorResponse> getAllVendors() {
        logger.info("Fetching all vendors");

        List<Vendor> allVendors = vendorRepository.findAll();

        List<PendingVendorResponse> response = allVendors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} vendors", response.size());
        return response;
    }

    /**
     * Approve a vendor by ID
     */
    @Transactional
    public PendingVendorResponse approveVendor(String vendorId) {
        logger.info("Approving vendor with ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vendor not found with id: " + vendorId));

        if (!"pending".equals(vendor.getState())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vendor is not in pending state. Current state: " + vendor.getState());
        }

        try {
            // Create Firebase user account for the vendor
            createVendorFirebaseAccount(vendor);

            // Update vendor state to approved
            vendor.setState("approved");
            Vendor savedVendor = vendorRepository.save(vendor);

            logger.info("Vendor approved successfully with Firebase account: {} (UID: {})",
                    vendor.getEmail(), vendor.getFirebaseUid());
            return mapToResponse(savedVendor);

        } catch (Exception e) {
            logger.error("Failed to approve vendor {}: {}", vendorId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to approve vendor: " + e.getMessage());
        }
    }

    /**
     * Create Firebase account for approved vendor
     */
    private void createVendorFirebaseAccount(Vendor vendor) {
        try {
            // Check if Firebase account already exists
            if (vendor.getFirebaseUid() != null && !vendor.getFirebaseUid().isEmpty()) {
                logger.info("Firebase account already exists for vendor: {} (UID: {})",
                        vendor.getEmail(), vendor.getFirebaseUid());
                return;
            }

            // Create Firebase user account
            UserRecord firebaseUser = firebaseUserService.createFirebaseUser(vendor.getEmail());

            logger.info("Firebase user created for vendor: {} with UID: {}",
                    vendor.getEmail(), firebaseUser.getUid());

            // Update vendor with Firebase UID
            vendor.setFirebaseUid(firebaseUser.getUid());

        } catch (Exception e) {
            logger.error("Failed to create Firebase account for vendor {}: {}",
                    vendor.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create Firebase account for vendor: " + e.getMessage());
        }
    }

    /**
     * Reject a vendor by ID
     */
    public PendingVendorResponse rejectVendor(String vendorId) {
        logger.info("Rejecting vendor with ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vendor not found with id: " + vendorId));

        if (!"pending".equals(vendor.getState())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vendor is not in pending state. Current state: " + vendor.getState());
        }

        vendor.setState("rejected");
        Vendor savedVendor = vendorRepository.save(vendor);

        logger.info("Vendor rejected successfully: {}", vendorId);
        return mapToResponse(savedVendor);
    }

    /**
     * Map Vendor entity to PendingVendorResponse DTO
     */
    private PendingVendorResponse mapToResponse(Vendor vendor) {
        return new PendingVendorResponse(
                vendor.getId(),
                vendor.getFirebaseUid(),
                vendor.getEmail(),
                vendor.getBusinessName(),
                vendor.getBusinessRegistrationNumber(),
                vendor.getContactNumber(),
                vendor.getBusinessType(),
                vendor.getCategories(),
                vendor.getState(),
                vendor.getCreatedAt(),
                vendor.getRole());
    }
}
