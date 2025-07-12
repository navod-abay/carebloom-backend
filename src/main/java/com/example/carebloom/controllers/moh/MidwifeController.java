package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Midwife;
import com.example.carebloom.services.moh.MidwifeService;
import com.example.carebloom.dto.CreateMidwifeRequest;
import com.example.carebloom.dto.UpdateMidwifeRequest;
import com.example.carebloom.dto.admin.MidwifeRequest;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "*")
public class MidwifeController {

    @Autowired
    private MidwifeService midwifeService;

    @GetMapping("/{officeId}/midwives")
    public ResponseEntity<List<Midwife>> getMidwivesByOffice(
            @PathVariable String officeId,
            Authentication authentication) {

        String firebaseUid = authentication.getName();
        List<Midwife> midwives = midwifeService.getAllMidwives(firebaseUid);
        return ResponseEntity.ok(midwives);
    }

    @PostMapping("/midwives")
    public ResponseEntity<Midwife> createMidwife(
            @RequestBody MidwifeRequest request,
            Authentication authentication) {

        String firebaseUid = authentication.getName();
        Midwife midwife = midwifeService.createMidwife(request, firebaseUid);
        return ResponseEntity.status(HttpStatus.CREATED).body(midwife);
    }

    @PutMapping("/{officeId}/midwives/{midwifeId}/approve")
    public ResponseEntity<Midwife> approveMidwife(
            @PathVariable String officeId,
            @PathVariable String midwifeId,
            Authentication authentication) {

        Midwife approvedMidwife = midwifeService.approveMidwife(midwifeId);
        return ResponseEntity.ok(approvedMidwife);
    }

    @PutMapping("/{midwifeId}")
    public ResponseEntity<Midwife> updateMidwife(
            @PathVariable String officeId,
            @PathVariable String midwifeId,
            @RequestBody MidwifeRequest request,
            Authentication authentication) {

        String firebaseUid = authentication.getName();
        Midwife updatedMidwife = midwifeService.updateMidwife(midwifeId, request, firebaseUid);
        return ResponseEntity.ok(updatedMidwife);
    }

    @DeleteMapping("/{midwifeId}")
    public ResponseEntity<Void> deleteMidwife(
            @PathVariable String officeId,
            @PathVariable String midwifeId,
            Authentication authentication) {

        String firebaseUid = authentication.getName();
        midwifeService.deleteMidwife(midwifeId, firebaseUid);
        return ResponseEntity.noContent().build();
    }
}