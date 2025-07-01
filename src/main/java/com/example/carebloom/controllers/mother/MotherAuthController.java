package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.LocationRegistrationRequest;
import com.example.carebloom.dto.PersonalRegistrationRequest;
import com.example.carebloom.dto.RegistrationRequest;
import com.example.carebloom.models.UserProfile;
import com.example.carebloom.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mothers/auth")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherAuthController {

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
            UserProfile profile = authService.registerMother(idToken, request.getEmail());
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/register/personal")
    public ResponseEntity<?> registerPersonal(
            @RequestHeader("Authorization") String idToken,
            @RequestBody PersonalRegistrationRequest request) {
        try {
            UserProfile profile = authService.updatePersonalInfo(idToken, request);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/register/location")
    public ResponseEntity<?> registerLocation(
            @RequestHeader("Authorization") String idToken,
            @RequestBody LocationRegistrationRequest request) {
        try {
            UserProfile profile = authService.updateLocation(idToken, request);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/skip-location")
    public ResponseEntity<?> skipLocation(
            @RequestHeader("Authorization") String idToken) {
        try {
            UserProfile profile = authService.skipLocation(idToken);
            return ResponseEntity.ok().body(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
