package com.app.controllers;

import com.app.services.AuthService;
import com.app.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/verify")
    public ResponseEntity<UserProfile> verifyToken(@RequestHeader("Authorization") String idToken) {
        UserProfile profile = authService.verifyIdToken(idToken);
        return ResponseEntity.ok(profile);
    }
}