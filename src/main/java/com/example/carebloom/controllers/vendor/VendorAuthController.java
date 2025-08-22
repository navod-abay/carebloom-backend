package com.example.carebloom.controllers.vendor;

import com.example.carebloom.models.Vendor;
import com.example.carebloom.repositories.VendorRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vendor/auth")
@CrossOrigin(origins = "${app.cors.vendor-origin}")
public class VendorAuthController {

    private static final Logger logger = LoggerFactory.getLogger(VendorAuthController.class);

    @Autowired
    private VendorRepository vendorRepository;

    /**
     * Verify Firebase token and check if the user is a vendor
     * This endpoint is used during sign-in before the user is fully authenticated
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            // Extract and verify the Firebase token
            String token = idToken.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String firebaseUid = decodedToken.getUid();

            logger.debug("Verifying token for Firebase UID: {}", firebaseUid);

            // Find the vendor by Firebase UID
            Vendor vendor = vendorRepository.findByFirebaseUid(firebaseUid);

            if (vendor == null) {
                logger.error("No vendor found for Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: User not found"));
            }

            // Create response with only role and userId
            Map<String, Object> response = new HashMap<>();
            response.put("role", "VENDOR");
            response.put("userId", vendor.getId());

            logger.info("Vendor authentication successful for: {}", vendor.getEmail());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }
}
