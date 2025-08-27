package com.example.carebloom.controllers.admin;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.carebloom.services.admin.PlatformAdminVendorService;

@RestController
@RequestMapping("/api/v1/vendor")
@CrossOrigin(origins = "${app.cors.moh-origin}")

public class PlatformAdminVendorController {
    private static final Logger logger = LoggerFactory.getLogger(PlatformAdminVendorController.class);

    @Autowired
    private PlatformAdminVendorService platformAdminVendorService;

    @PostMapping("/vendors/{vendorId}/accept")
    public ResponseEntity<?> acceptVendorRegistration(@RequestBody Map<String, String> request,
            @PathVariable String vendorId) {
        try {
            platformAdminVendorService.acceptVendorRegistration(vendorId);
            return ResponseEntity.ok(Map.of("message", "Vendor registration accepted"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            logger.error("Error accepting vendor registration for ID: {}", vendorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to accept vendor registration: " + e.getMessage()));
        }
    }

}
