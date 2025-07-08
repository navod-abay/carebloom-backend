package com.example.carebloom.controllers.admin;

import com.example.carebloom.models.UserProfile;
import com.example.carebloom.models.PlatformAdmin;
import com.example.carebloom.repositories.PlatformAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/admin/auth")
@CrossOrigin(origins = "${app.cors.admin-origin}")
public class PlatformAdminAuthController {

    private static final Logger logger = LoggerFactory.getLogger(PlatformAdminAuthController.class);

    @Autowired
    private PlatformAdminRepository platformAdminRepository;
    
    /**
     * Get the authenticated user profile directly from the security context
     * This avoids redundant token verification since the RoleAuthenticationFilter
     * has already verified the token and set the authentication
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(Authentication authentication) {
        try {
            // The authentication principal is the Firebase UID (from RoleAuthenticationFilter)
            String firebaseUid = authentication.getName();
            logger.debug("Getting user profile from security context for UID: {}", firebaseUid);
            
            // Find the admin by Firebase UID
            PlatformAdmin admin = platformAdminRepository.findByFirebaseUid(firebaseUid);
            
            if (admin == null) {
                logger.error("No admin found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }
            
            // Create and return user profile
            UserProfile profile = new UserProfile();
            profile.setId(admin.getId());
            profile.setName(admin.getName());
            profile.setEmail(admin.getEmail());
            profile.setRole(admin.getRole());
            
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            logger.error("Error retrieving profile: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
