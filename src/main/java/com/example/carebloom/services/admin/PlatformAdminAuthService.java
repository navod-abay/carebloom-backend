package com.example.carebloom.services.admin;

import com.example.carebloom.models.PlatformAdmin;
import com.example.carebloom.models.UserProfile;
import com.example.carebloom.repositories.PlatformAdminRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PlatformAdminAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlatformAdminAuthService.class);
    
    @Autowired
    private PlatformAdminRepository platformAdminRepository;

    private UserProfile createUserProfile(PlatformAdmin admin) {
        UserProfile profile = new UserProfile();
        profile.setId(admin.getId());
        profile.setName(admin.getName());
        profile.setEmail(admin.getEmail());
        profile.setRole(admin.getRole());
        return profile;
    }

    public UserProfile verifyIdToken(String idToken) throws Exception {
        String token = idToken.replace("Bearer ", "");
        logger.debug("Verifying token: {}", token.substring(0, Math.min(10, token.length())) + "...");
        
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        logger.debug("Token decoded successfully. UID: {}, Email: {}, Name: {}", 
            decodedToken.getUid(),
            decodedToken.getEmail(),
            decodedToken.getName());
        
        PlatformAdmin admin = platformAdminRepository.findByFirebaseUid(decodedToken.getUid());
        if (admin == null) {
            logger.error("No admin found for Firebase UID: {}", decodedToken.getUid());
            throw new RuntimeException("Unauthorized: User not found");
        }
        
        logger.debug("Admin found: {}", admin.getEmail());
        return createUserProfile(admin);
    }
}
