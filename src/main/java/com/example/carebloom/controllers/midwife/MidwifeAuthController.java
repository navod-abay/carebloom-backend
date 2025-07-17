package com.example.carebloom.controllers.midwife;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.carebloom.dto.midwife.MidwifeMidDTO;
import com.example.carebloom.dto.mother.MotherMidDTO;
import com.example.carebloom.dto.unit.UnitBasicDTO;
import com.example.carebloom.models.Unit;
import com.example.carebloom.repositories.MidwifeRepository;
import com.example.carebloom.repositories.UnitRepository;
import com.example.carebloom.services.midwife.MidwifeAuthService;
import com.example.carebloom.models.Midwife;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "${app.cors.midwife-origin}")
@RequestMapping("/api/v1/midwife")
public class MidwifeAuthController {

    private static final Logger logger = LoggerFactory.getLogger(MidwifeAuthController.class);

    @Autowired
    private MidwifeRepository midwifeRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private MidwifeAuthService midwifeAuthService;

    @PostMapping("/auth/verify")
    public ResponseEntity<?> verifyMidwife() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                logger.debug("Midwife is not authenticated or anonymous");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not signed in properly");
            }

            String firebaseUid = authentication.getName();
            logger.debug("Retrieved Firebase UID for midwife: {}", firebaseUid);

            // Find midwife by Firebase UID
            Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
            if (midwife == null) {
                logger.debug("No midwife found for Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Midwife not found");
            }

            // Get units assigned to this midwife's office
            List<Unit> units = unitRepository.findByMohOfficeId(midwife.getOfficeId());
            
            // Convert units to UnitBasicDTO
            List<UnitBasicDTO> unitDtos = units.stream()
                .map(unit -> {
                    UnitBasicDTO dto = new UnitBasicDTO();
                    dto.setId(unit.getId());
                    dto.setName(unit.getName());
                    return dto;
                })
                .collect(Collectors.toList());

            // Create and populate MidwifeMidDTO
            MidwifeMidDTO midwifeDto = new MidwifeMidDTO();
            midwifeDto.setId(midwife.getId());
            midwifeDto.setName(midwife.getName());
            midwifeDto.setMohOfficeId(midwife.getOfficeId());
            midwifeDto.setUnits(unitDtos);

            logger.info("Midwife verification successful for: {}", midwife.getName());
            return ResponseEntity.ok(midwifeDto);

        } catch (Exception e) {
            logger.error("Error verifying midwife: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to verify midwife: " + e.getMessage());
        }
    }

    @GetMapping("/assigned-mothers")
    public ResponseEntity<?> getAssignedMothers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                logger.debug("Midwife is not authenticated or anonymous");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not signed in properly");
            }

            String firebaseUid = authentication.getName();
            logger.debug("Getting assigned mothers for midwife with Firebase UID: {}", firebaseUid);

            List<MotherMidDTO> assignedMothers = midwifeAuthService.getAssignedMothers(firebaseUid);
            
            logger.info("Retrieved {} assigned mothers for midwife: {}", assignedMothers.size(), firebaseUid);
            return ResponseEntity.ok(assignedMothers);

        } catch (Exception e) {
            logger.error("Error getting assigned mothers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to get assigned mothers: " + e.getMessage());
        }
    }
}
