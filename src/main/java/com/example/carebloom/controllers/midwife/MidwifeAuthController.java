package com.example.carebloom.controllers.midwife;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.carebloom.models.UserProfile;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.models.Midwife;;

@RestController
@CrossOrigin(origins = "${app.cors.midwife-origin}")
@RequestMapping("/api/v1/midwife/auth")
public class MidwifeAuthController {

    private static final Logger logger = LoggerFactory.getLogger(MidwifeAuthController.class);

    @Autowired
    private  MidwifeRepository midwifeRepository;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            // Extract the Firebase UID from the token
            String token = idToken.replace("Bearer ", "");
            com.google.firebase.auth.FirebaseToken decodedToken = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .verifyIdToken(token);
            String firebaseUid = decodedToken.getUid();
            logger.debug("Verified token for UID: {}", firebaseUid);

            // Find the MoH user by Firebase UID
            Midwife mohUser = midwifeRepository.findByFirebaseUid(firebaseUid);

            if (mohUser == null) {
                logger.error("No MoH user found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(401).body("Unauthorized: User not found");
            }

            // Create and return user profile
            UserProfile profile = new UserProfile();
            profile.setId(mohUser.getId());
            profile.setName(mohUser.getName());
            profile.setEmail(mohUser.getEmail());
            profile.setOfficeId(mohUser.getOfficeId()); // Include office ID

            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            logger.error("Error retrieving profile: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

}
