package com.example.carebloom.controllers.admin;

import com.example.carebloom.models.PlatformAdmin;
import com.example.carebloom.repositories.PlatformAdminRepository;
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
@RequestMapping("/api/v1/admin/auth")
@CrossOrigin(origins = "${app.cors.admin-origin}")
public class PlatformAdminAuthController {

    private static final Logger logger = LoggerFactory.getLogger(PlatformAdminAuthController.class);

    @Autowired
    private PlatformAdminRepository platformAdminRepository;
    
    /**
     * Verify Firebase token and check if the user is a platform admin
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
            
            // Find the admin by Firebase UID
            PlatformAdmin admin = platformAdminRepository.findByFirebaseUid(firebaseUid);
            
            if (admin == null) {
                logger.error("No platform admin found for Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: User not found"));
            }
            
            // Create response with only role and userId
            Map<String, Object> response = new HashMap<>();
            response.put("role", "PLATFORM_MANAGER");
            response.put("userId", admin.getId());
            
            logger.info("Platform admin authentication successful for: {}", admin.getEmail());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }
}
