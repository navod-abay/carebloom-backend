package com.example.carebloom.controllers;

import com.example.carebloom.dto.RegistrationRequest;
import com.example.carebloom.models.UserProfile;
import com.example.carebloom.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = authService.verifyIdToken(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerMother(
            @RequestHeader("Authorization") String idToken,
            @RequestBody RegistrationRequest request) {
        try {
            authService.registerMother(idToken, request.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
