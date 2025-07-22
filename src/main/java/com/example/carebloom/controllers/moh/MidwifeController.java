package com.example.carebloom.controllers.moh;

import com.example.carebloom.dto.midwife.MidwifeBasicDTO;
import com.example.carebloom.dto.midwife.MidwifeExtendedDTO;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.services.moh.MidwifeService;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MidwifeController {

    @Autowired
    private MidwifeService midwifeService;

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    @GetMapping("/midwives")
    public ResponseEntity<List<MidwifeBasicDTO>> getMidwivesByOffice(Authentication authentication) {

        String officeId = getUserOfficeId(authentication.getName());


        List<MidwifeBasicDTO> midwives = midwifeRepository.findBasicDetailsByOfficeId(officeId);
        return ResponseEntity.ok(midwives);
    }

    @PostMapping("/midwife")
    public ResponseEntity<Midwife> createMidwife(
            @RequestBody MidwifeBasicDTO request,
            Authentication authentication) {

        String firebaseUid = authentication.getName();
        Midwife midwife = midwifeService.createMidwife(request, firebaseUid);
        return ResponseEntity.status(HttpStatus.CREATED).body(midwife);
    }

    @PutMapping("/{midwifeId}")
    public ResponseEntity<Midwife> updateMidwife(
            @PathVariable String officeId,
            @PathVariable String midwifeId,
            @RequestBody MidwifeBasicDTO request,
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

    @GetMapping("/midwife/{midwifeId}")
    public ResponseEntity<MidwifeExtendedDTO> getMidwifeExtendedDetails(
            @PathVariable String midwifeId,
            Authentication authentication) {
        String firebaseUid = authentication.getName();
        MidwifeExtendedDTO dto = midwifeService.getMidwifeExtendedDetails(midwifeId, firebaseUid);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/midwife/{midwifeId}/grant-access")
    public ResponseEntity<Void> grantAccessToMidwife(
            @PathVariable String midwifeId,
            Authentication authentication) {

        midwifeService.grantAccessToMidwife(midwifeId);
        return ResponseEntity.noContent().build();
    }

    private String getUserOfficeId(String firebaseUid) {
        MoHOfficeUser user = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        if (!"active".equals(user.getState())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }

        return user.getOfficeId();
    }
}