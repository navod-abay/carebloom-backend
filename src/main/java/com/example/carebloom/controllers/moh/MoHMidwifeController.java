package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Midwife;
import com.example.carebloom.dto.admin.MidwifeRequest;
import com.example.carebloom.services.moh.MidwifeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/moh/midwives")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MoHMidwifeController {

    @Autowired
    private MidwifeService midwifeService;

    /**
     * Get all midwives for the MOH office
     */
    @GetMapping
    public ResponseEntity<List<Midwife>> getAllMidwives(Authentication authentication) {
        String firebaseUid = authentication.getName();
        List<Midwife> midwives = midwifeService.getAllMidwives(firebaseUid);
        return ResponseEntity.ok(midwives);
    }

    /**
     * Get a specific midwife by ID
     */
    @GetMapping("/{midwifeId}")
    public ResponseEntity<Midwife> getMidwifeById(
            @PathVariable String midwifeId,
            Authentication authentication) {
        String firebaseUid = authentication.getName();
        Midwife midwife = midwifeService.getMidwifeById(midwifeId, firebaseUid);
        return ResponseEntity.ok(midwife);
    }

    /**
     * Create a new midwife
     */
    @PostMapping
    public ResponseEntity<Midwife> createMidwife(
            @RequestBody MidwifeRequest request,
            Authentication authentication) {
        String firebaseUid = authentication.getName();
        Midwife createdMidwife = midwifeService.createMidwife(request, firebaseUid);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMidwife);
    }

    /**
     * Update an existing midwife
     */
    @PutMapping("/{midwifeId}")
    public ResponseEntity<Midwife> updateMidwife(
            @PathVariable String midwifeId,
            @RequestBody MidwifeRequest request,
            Authentication authentication) {
        String firebaseUid = authentication.getName();
        Midwife updatedMidwife = midwifeService.updateMidwife(midwifeId, request, firebaseUid);
        return ResponseEntity.ok(updatedMidwife);
    }

    /**
     * Delete a midwife
     */
    @DeleteMapping("/{midwifeId}")
    public ResponseEntity<Void> deleteMidwife(
            @PathVariable String midwifeId,
            Authentication authentication) {
        String firebaseUid = authentication.getName();
        midwifeService.deleteMidwife(midwifeId, firebaseUid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get midwives by clinic
     */
    @GetMapping("/by-clinic/{clinic}")
    public ResponseEntity<List<Midwife>> getMidwivesByClinic(
            @PathVariable String clinic,
            Authentication authentication) {
        String firebaseUid = authentication.getName();
        List<Midwife> midwives = midwifeService.getMidwivesByClinic(clinic, firebaseUid);
        return ResponseEntity.ok(midwives);
    }

    /**
     * Get midwives by specialization
     */
    @GetMapping("/by-specialization/{specialization}")
    public ResponseEntity<List<Midwife>> getMidwivesBySpecialization(
            @PathVariable String specialization,
            Authentication authentication) {
        String firebaseUid = authentication.getName();
        List<Midwife> midwives = midwifeService.getMidwivesBySpecialization(specialization, firebaseUid);
        return ResponseEntity.ok(midwives);
    }

    /**
     * Get available clinics (distinct list)
     */
    @GetMapping("/clinics")
    public ResponseEntity<List<String>> getAvailableClinics(Authentication authentication) {
        String firebaseUid = authentication.getName();
        List<Midwife> midwives = midwifeService.getAllMidwives(firebaseUid);
        List<String> clinics = midwives.stream()
                .map(Midwife::getClinic)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(clinics);
    }

    /**
     * Get available specializations (distinct list)
     */
    @GetMapping("/specializations")
    public ResponseEntity<List<String>> getAvailableSpecializations(Authentication authentication) {
        String firebaseUid = authentication.getName();
        List<Midwife> midwives = midwifeService.getAllMidwives(firebaseUid);
        List<String> specializations = midwives.stream()
                .map(Midwife::getSpecialization)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(specializations);
    }
}
