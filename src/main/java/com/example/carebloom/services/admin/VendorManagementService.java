package com.example.carebloom.services.admin;

import com.example.carebloom.dto.admin.PendingVendorResponse;
import com.example.carebloom.models.Vendor;
import com.example.carebloom.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorManagementService {

    private static final Logger logger = LoggerFactory.getLogger(VendorManagementService.class);

    @Autowired
    private VendorRepository vendorRepository;

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
    public PendingVendorResponse approveVendor(String vendorId) {
        logger.info("Approving vendor with ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        vendor.setState("approved");
        Vendor savedVendor = vendorRepository.save(vendor);

        logger.info("Vendor approved successfully: {}", vendorId);
        return mapToResponse(savedVendor);
    }

    /**
     * Reject a vendor by ID
     */
    public PendingVendorResponse rejectVendor(String vendorId) {
        logger.info("Rejecting vendor with ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

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
