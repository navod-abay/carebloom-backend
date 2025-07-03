package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.UserProfile;
import com.example.carebloom.services.moh.MoHAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/moh/auth")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MoHAuthController {

    @Autowired
    private MoHAuthService mohAuthService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = mohAuthService.verifyIdToken(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = mohAuthService.signInUser(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/role")
    public ResponseEntity<?> getUserRole(@RequestHeader("Authorization") String idToken) {
        try {
            String token = idToken.replace("Bearer ", "");
            UserProfile profile = mohAuthService.verifyIdToken(idToken);

            // Get additional role information
            boolean isAdmin = mohAuthService.isUserAdmin(profile.getId());
            boolean isActive = mohAuthService.isUserActive(profile.getId());

            return ResponseEntity.ok().body(Map.of(
                    "profile", profile,
                    "isAdmin", isAdmin,
                    "isActive", isActive
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = mohAuthService.verifyIdToken(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
