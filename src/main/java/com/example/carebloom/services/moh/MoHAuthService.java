package com.example.carebloom.services.moh;

import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.models.UserProfile;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MoHAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(MoHAuthService.class);
    
    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    private UserProfile createUserProfile(MoHOfficeUser mohUser) {
        UserProfile profile = new UserProfile();
        profile.setId(mohUser.getId());
        profile.setName(mohUser.getName());
        profile.setEmail(mohUser.getEmail());
        profile.setRole(mohUser.getRole());
        return profile;
    }

    public UserProfile verifyIdToken(String idToken) throws Exception {
        String token = idToken.replace("Bearer ", "");
        logger.debug("Verifying token for MoH user: {}", token.substring(0, Math.min(10, token.length())) + "...");
        
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        logger.debug("Token decoded successfully. UID: {}, Email: {}", 
            decodedToken.getUid(),
            decodedToken.getEmail());
        
        MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(decodedToken.getUid());
        if (mohUser == null) {
            logger.error("No MoH user found for Firebase UID: {}", decodedToken.getUid());
            throw new RuntimeException("Unauthorized: User not found");
        }
        
        if (!"active".equals(mohUser.getState())) {
            logger.error("MoH user {} is not active. Current state: {}", mohUser.getEmail(), mohUser.getState());
            throw new RuntimeException("Unauthorized: User account is not active");
        }
        
        logger.debug("MoH user found: {}", mohUser.getEmail());
        return createUserProfile(mohUser);
    }

    public UserProfile signInUser(String idToken) throws Exception {
        // For MoH users, sign-in is the same as verify
        // You can add additional sign-in logic here if needed
        return verifyIdToken(idToken);
    }

    public boolean isUserAdmin(String firebaseUid) throws Exception {
        MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (mohUser == null) {
            throw new RuntimeException("User not found");
        }
        return "admin".equals(mohUser.getAccountType());
    }

    public boolean isUserActive(String firebaseUid) throws Exception {
        MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (mohUser == null) {
            throw new RuntimeException("User not found");
        }
        return "active".equals(mohUser.getState());
    }

    public MoHOfficeUser getUserByFirebaseUid(String firebaseUid) throws Exception {
        MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (mohUser == null) {
            throw new RuntimeException("User not found");
        }
        return mohUser;
    }
}
