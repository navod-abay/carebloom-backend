package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Clinic;
import com.example.carebloom.models.UserProfile;
import com.example.carebloom.services.moh.MoHClinicService;
import com.example.carebloom.services.moh.MoHAuthService;
import com.example.carebloom.dto.CreateClinicResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "{app.cors.moh-origin}")
public class MoHClinicController {

    @Autowired
    private MoHClinicService clinicService;
    
    @Autowired
    private MoHAuthService mohAuthService;

    @GetMapping("/clinics")
    public ResponseEntity<List<Clinic>> getAllClinics() {
        List<Clinic> clinics = clinicService.getAllClinics();
        return ResponseEntity.ok(clinics);
    }

    @GetMapping("/clinics/date/{date}")
    public ResponseEntity<List<Clinic>> getClinicsByDate(@PathVariable String date) {
        List<Clinic> clinics = clinicService.getClinicsByDate(date);
        return ResponseEntity.ok(clinics);
    }

    @GetMapping("/clinics/{id}")
    public ResponseEntity<Clinic> getClinicById(@PathVariable String id) {
        Optional<Clinic> clinic = clinicService.getClinicById(id);
        return clinic.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clinics")
    public ResponseEntity<CreateClinicResponse> createClinic(
            @RequestBody Clinic clinic,
            @RequestHeader("Authorization") String idToken) {
        try {
            // Extract user ID from Firebase authentication token
            UserProfile userProfile = mohAuthService.verifyIdToken(idToken);
            String userId = userProfile.getId();
            
            CreateClinicResponse response = clinicService.createClinic(clinic, userId);
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            CreateClinicResponse errorResponse = new CreateClinicResponse(false, "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PutMapping("/clinics/{id}")
    public ResponseEntity<Clinic> updateClinic(@PathVariable String id, @RequestBody Clinic clinic) {
        Clinic updatedClinic = clinicService.updateClinic(id, clinic);
        if (updatedClinic != null) {
            return ResponseEntity.ok(updatedClinic);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/clinics/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable String id) {
        boolean deleted = clinicService.deleteClinic(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/my-clinics")
    public ResponseEntity<List<Clinic>> getMyClinics(@RequestHeader("Authorization") String idToken) {
        try {
            // Extract user ID from Firebase authentication token
            UserProfile userProfile = mohAuthService.verifyIdToken(idToken);
            String userId = userProfile.getId();
            
            List<Clinic> clinics = clinicService.getClinicsByUserId(userId);
            return ResponseEntity.ok(clinics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
