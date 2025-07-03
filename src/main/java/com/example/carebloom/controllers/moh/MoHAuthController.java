package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.UserProfile;
import com.example.carebloom.services.moh.MoHAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = mohAuthService.signInUser(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
