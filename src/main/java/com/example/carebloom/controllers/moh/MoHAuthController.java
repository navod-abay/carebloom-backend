package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.UserProfile;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/moh/auth")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MoHAuthController {

    private static final Logger logger = LoggerFactory.getLogger(MoHAuthController.class);

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;
    


    /**
     * Verify token and get user profile
     * This endpoint is public and needs to extract the Firebase UID from the token
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            // Extract the Firebase UID from the token
            String token = idToken.replace("Bearer ", "");
            com.google.firebase.auth.FirebaseToken decodedToken = com.google.firebase.auth.FirebaseAuth.getInstance().verifyIdToken(token);
            String firebaseUid = decodedToken.getUid();
            logger.debug("Verified token for UID: {}", firebaseUid);
            
            // Find the MoH user by Firebase UID
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
            
            if (mohUser == null) {
                logger.error("No MoH user found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }
            
            // Create and return user profile
            UserProfile profile = new UserProfile();
            profile.setId(mohUser.getId());
            profile.setName(mohUser.getName());
            profile.setEmail(mohUser.getEmail());
            profile.setRole(mohUser.getRole());
            
            // Return the profile with officeId
            return ResponseEntity.ok().body(Map.of(
                "profile", profile,
                "officeId", mohUser.getOfficeId()
            ));
        } catch (Exception e) {
            logger.error("Error retrieving profile: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    /**
     * Sign in a MoH user using the authentication context
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(Authentication authentication) {
        try {
            String firebaseUid = authentication.getName();
            logger.debug("Signing in user from security context for UID: {}", firebaseUid);
            
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
            
            if (mohUser == null) {
                logger.error("No MoH user found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }
            
            if (!"active".equals(mohUser.getState())) {
                logger.error("MoH user {} is not active. Current state: {}", mohUser.getEmail(), mohUser.getState());
                return ResponseEntity.status(403).body("Unauthorized: User account is not active");
            }
            
            // Create and return user profile
            UserProfile profile = new UserProfile();
            profile.setId(mohUser.getId());
            profile.setName(mohUser.getName());
            profile.setEmail(mohUser.getEmail());
            profile.setRole(mohUser.getRole());
            
            // Return the profile with officeId
            return ResponseEntity.ok().body(Map.of(
                "profile", profile,
                "officeId", mohUser.getOfficeId()
            ));
        } catch (Exception e) {
            logger.error("Error signing in: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    /**
     * Get user role and additional information using the security context
     */
    @GetMapping("/role")
    public ResponseEntity<?> getUserRole(Authentication authentication) {
        try {
            String firebaseUid = authentication.getName();
            logger.debug("Getting user role from security context for UID: {}", firebaseUid);
            
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
            
            if (mohUser == null) {
                logger.error("No MoH user found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }
            
            // Create user profile
            UserProfile profile = new UserProfile();
            profile.setId(mohUser.getId());
            profile.setName(mohUser.getName());
            profile.setEmail(mohUser.getEmail());
            profile.setRole(mohUser.getRole());
            
            // Determine admin status and active status
            boolean isAdmin = "admin".equals(mohUser.getAccountType());
            boolean isActive = "active".equals(mohUser.getState());

            return ResponseEntity.ok().body(Map.of(
                    "profile", profile,
                    "isAdmin", isAdmin,
                    "isActive", isActive
            ));
        } catch (Exception e) {
            logger.error("Error retrieving role: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    /**
     * Get user information using the security context
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        try {
            String firebaseUid = authentication.getName();
            logger.debug("Getting user info from security context for UID: {}", firebaseUid);
            
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
            
            if (mohUser == null) {
                logger.error("No MoH user found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }
            
            // Create and return user profile
            UserProfile profile = new UserProfile();
            profile.setId(mohUser.getId());
            profile.setName(mohUser.getName());
            profile.setEmail(mohUser.getEmail());
            profile.setRole(mohUser.getRole());
            
            // Return the profile with officeId
            return ResponseEntity.ok().body(Map.of(
                "profile", profile,
                "officeId", mohUser.getOfficeId()
            ));
        } catch (Exception e) {
            logger.error("Error retrieving user info: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
