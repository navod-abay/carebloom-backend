package com.example.carebloom.services;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Generate and send sign-in link email to vendor
     */
    public void sendVendorSignInLinkEmail(String email, String businessName) {
        try {
            // Create action code settings for email link
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.builder()
                    .setUrl("http://localhost:5176/set-password?email=" + email) // Vendor frontend URL
                    .setHandleCodeInApp(true)
                    .build();

            // Generate email verification link
            String link = FirebaseAuth.getInstance().generateSignInWithEmailLink(email, actionCodeSettings);

            // Log the generated link (in production, you would send this via email service)
            logger.info("Sign-in link generated for vendor {}: {}", email, link);

            // TODO: Replace with actual email sending service (SendGrid, AWS SES, etc.)
            // For now, just log the link
            logger.info("=".repeat(80));
            logger.info("VENDOR SIGN-IN LINK EMAIL");
            logger.info("To: {}", email);
            logger.info("Subject: Welcome to CareBloom - Set Your Password");
            logger.info("Business: {}", businessName);
            logger.info("Sign-in Link: {}", link);
            logger.info("=".repeat(80));

        } catch (Exception e) {
            logger.error("Failed to send sign-in link email to vendor {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send email to vendor: " + e.getMessage());
        }
    }

    /**
     * Send confirmation email to vendor after registration
     */
    public void sendVendorRegistrationConfirmation(String email, String businessName) {
        try {
            logger.info("=".repeat(80));
            logger.info("VENDOR REGISTRATION CONFIRMATION EMAIL");
            logger.info("To: {}", email);
            logger.info("Subject: Registration Received - CareBloom");
            logger.info("Business: {}", businessName);
            logger.info("Message: Your vendor registration has been received and is pending approval.");
            logger.info("=".repeat(80));

        } catch (Exception e) {
            logger.error("Failed to send registration confirmation to vendor {}: {}", email, e.getMessage());
            // Don't throw exception for confirmation email failure
        }
    }
}
