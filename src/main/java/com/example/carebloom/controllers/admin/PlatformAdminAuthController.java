package com.example.carebloom.controllers.admin;

import com.example.carebloom.models.UserProfile;
import com.example.carebloom.services.admin.PlatformAdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class PlatformAdminAuthController {

    @Autowired
    private PlatformAdminAuthService authService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = authService.verifyIdToken(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
