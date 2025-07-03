package com.example.carebloom.services.admin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FirebaseUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseUserService.class);
    
    public UserRecord createFirebaseUser(String email) throws Exception {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setEmailVerified(false);
                
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            logger.info("Successfully created Firebase user with UID: {}", userRecord.getUid());
            return userRecord;
            
        } catch (Exception e) {
            logger.error("Failed to create Firebase user for email: {}", email, e);
            throw new RuntimeException("Failed to create Firebase user: " + e.getMessage());
        }
    }
}
