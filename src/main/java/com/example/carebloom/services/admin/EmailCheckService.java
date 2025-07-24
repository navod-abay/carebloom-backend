package com.example.carebloom.services.admin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailCheckService {

    private static final Logger logger = LoggerFactory.getLogger(EmailCheckService.class);

    /**
     * Check if an email is already registered in Firebase Authentication
     * 
     * @param email The email to check
     * @return true if the email exists in Firebase Auth, false otherwise
     */
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        logger.debug("Checking if email exists in Firebase Auth: {}", email);

        try {
            // Try to get the user by email
            var userRecords = FirebaseAuth.getInstance().getUserByEmailAsync(email).get();
            // If we get here, the user exists
            logger.debug("Email {} exists in Firebase Auth", userRecords.getEmail());
            return true;
        } catch (Exception e) {
            Throwable cause = e;
            // Unwrap causes to find FirebaseAuthException
            while (cause != null) {
                if (cause instanceof FirebaseAuthException) {
                    FirebaseAuthException authException = (FirebaseAuthException) cause;
                    if ("user-not-found".equals(authException.getErrorCode())) {
                        logger.debug("Email {} does not exist in Firebase Auth", email);
                        return false;
                    }
                }
                cause = cause.getCause();
            }
            // For other exceptions, log and return false
            logger.error("Error checking email in Firebase: {}", e.getMessage());
            return false;
        }
    }
}
