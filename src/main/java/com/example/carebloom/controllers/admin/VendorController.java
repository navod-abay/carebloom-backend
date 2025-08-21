package com.example.carebloom.controllers.admin;

import com.example.carebloom.dto.admin.PendingVendorResponse;
import com.example.carebloom.services.admin.VendorManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/vendors")
@CrossOrigin(origins = "${app.cors.admin-origin}")
public class VendorController {

    private static final Logger logger = LoggerFactory.getLogger(VendorController.class);

    @Autowired
    private VendorManagementService vendorManagementService;

    /**
     * Get all pending vendors for admin approval
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingVendors() {
        try {
            List<PendingVendorResponse> pendingVendors = vendorManagementService.getAllPendingVendors();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", pendingVendors,
                    "count", pendingVendors.size(),
                    "message", "Pending vendors retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching pending vendors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch pending vendors: " + e.getMessage()));
        }
    }

    /**
     * Get all vendors regardless of status
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllVendors() {
        try {
            List<PendingVendorResponse> allVendors = vendorManagementService.getAllVendors();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", allVendors,
                    "count", allVendors.size(),
                    "message", "All vendors retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching all vendors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch vendors: " + e.getMessage()));
        }
    }

    /**
     * Approve a vendor by ID
     */
    @PostMapping("/{vendorId}/approve")
    public ResponseEntity<?> approveVendor(@PathVariable String vendorId) {
        try {
            PendingVendorResponse approvedVendor = vendorManagementService.approveVendor(vendorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", approvedVendor,
                    "message", "Vendor approved successfully"));
        } catch (RuntimeException e) {
            logger.error("Error approving vendor with ID: {}", vendorId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error approving vendor with ID: {}", vendorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to approve vendor: " + e.getMessage()));
        }
    }

    /**
     * Reject a vendor by ID
     */
    @PostMapping("/{vendorId}/reject")
    public ResponseEntity<?> rejectVendor(@PathVariable String vendorId) {
        try {
            PendingVendorResponse rejectedVendor = vendorManagementService.rejectVendor(vendorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", rejectedVendor,
                    "message", "Vendor rejected successfully"));
        } catch (RuntimeException e) {
            logger.error("Error rejecting vendor with ID: {}", vendorId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error rejecting vendor with ID: {}", vendorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to reject vendor: " + e.getMessage()));
        }
    }
}
