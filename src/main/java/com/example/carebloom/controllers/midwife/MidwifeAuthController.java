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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

import java.time.LocalDateTime;
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

    @GetMapping("/profile")
    public ResponseEntity<?> getMidwifeProfile() {
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
            logger.debug("Getting profile for midwife with Firebase UID: {}", firebaseUid);

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

            // Create profile response with all midwife data
            java.util.Map<String, Object> profileData = new java.util.HashMap<>();
            profileData.put("id", midwife.getId());
            profileData.put("name", midwife.getName());
            profileData.put("email", midwife.getEmail());
            profileData.put("phone", midwife.getPhone());
            profileData.put("mohOfficeId", midwife.getOfficeId());
            profileData.put("state", midwife.getState());
            profileData.put("units", unitDtos);
            profileData.put("assignedMotherCount",
                    midwife.getAssignedMotherIds() != null ? midwife.getAssignedMotherIds().size() : 0);
            profileData.put("createdAt", midwife.getCreatedAt());
            profileData.put("updatedAt", midwife.getUpdatedAt());
            
            // Add new profile fields
            profileData.put("address", midwife.getAddress());
            profileData.put("profileImage", midwife.getProfileImage());
            profileData.put("specialization", midwife.getSpecialization());
            profileData.put("experience", midwife.getExperience());
            profileData.put("licenseNumber", midwife.getLicenseNumber());
            profileData.put("workingHours", midwife.getWorkingHours());
            profileData.put("bio", midwife.getBio());

            logger.info("Profile retrieved successfully for midwife: {}", midwife.getName());
            return ResponseEntity.ok(profileData);

        } catch (Exception e) {
            logger.error("Error getting midwife profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get midwife profile: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateMidwifeProfile(@RequestBody java.util.Map<String, Object> updateData) {
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
            logger.debug("Updating profile for midwife with Firebase UID: {}", firebaseUid);

            // Find midwife by Firebase UID
            Midwife midwife = midwifeRepository.findByFirebaseUid(firebaseUid);
            if (midwife == null) {
                logger.debug("No midwife found for Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Midwife not found");
            }

            // Update only editable fields
            if (updateData.containsKey("name")) {
                midwife.setName((String) updateData.get("name"));
            }
            if (updateData.containsKey("email")) {
                midwife.setEmail((String) updateData.get("email"));
            }
            if (updateData.containsKey("phone")) {
                midwife.setPhone((String) updateData.get("phone"));
            }
            if (updateData.containsKey("address")) {
                midwife.setAddress((String) updateData.get("address"));
            }
            if (updateData.containsKey("profileImage")) {
                midwife.setProfileImage((String) updateData.get("profileImage"));
            }
            if (updateData.containsKey("specialization")) {
                midwife.setSpecialization((String) updateData.get("specialization"));
            }
            if (updateData.containsKey("experience")) {
                Object experienceObj = updateData.get("experience");
                if (experienceObj != null) {
                    if (experienceObj instanceof Integer) {
                        midwife.setExperience((Integer) experienceObj);
                    } else if (experienceObj instanceof String) {
                        try {
                            midwife.setExperience(Integer.parseInt((String) experienceObj));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid experience value: {}", experienceObj);
                        }
                    } else if (experienceObj instanceof Number) {
                        midwife.setExperience(((Number) experienceObj).intValue());
                    }
                }
            }
            if (updateData.containsKey("licenseNumber")) {
                midwife.setLicenseNumber((String) updateData.get("licenseNumber"));
            }
            if (updateData.containsKey("workingHours")) {
                midwife.setWorkingHours((String) updateData.get("workingHours"));
            }
            if (updateData.containsKey("bio")) {
                midwife.setBio((String) updateData.get("bio"));
            }

            // Save updated midwife
            midwife.setUpdatedAt(LocalDateTime.now());
            Midwife updatedMidwife = midwifeRepository.save(midwife);

            // Get units for the response
            List<Unit> units = unitRepository.findByMohOfficeId(updatedMidwife.getOfficeId());
            List<UnitBasicDTO> unitDtos = units.stream()
                    .map(unit -> {
                        UnitBasicDTO dto = new UnitBasicDTO();
                        dto.setId(unit.getId());
                        dto.setName(unit.getName());
                        return dto;
                    })
                    .collect(Collectors.toList());

            // Create response with updated data
            java.util.Map<String, Object> profileData = new java.util.HashMap<>();
            profileData.put("id", updatedMidwife.getId());
            profileData.put("name", updatedMidwife.getName());
            profileData.put("email", updatedMidwife.getEmail());
            profileData.put("phone", updatedMidwife.getPhone());
            profileData.put("address", updatedMidwife.getAddress());
            profileData.put("profileImage", updatedMidwife.getProfileImage());
            profileData.put("specialization", updatedMidwife.getSpecialization());
            profileData.put("experience", updatedMidwife.getExperience());
            profileData.put("licenseNumber", updatedMidwife.getLicenseNumber());
            profileData.put("workingHours", updatedMidwife.getWorkingHours());
            profileData.put("bio", updatedMidwife.getBio());
            profileData.put("mohOfficeId", updatedMidwife.getOfficeId());
            profileData.put("state", updatedMidwife.getState());
            profileData.put("units", unitDtos);
            profileData.put("assignedMotherCount",
                    updatedMidwife.getAssignedMotherIds() != null ? updatedMidwife.getAssignedMotherIds().size() : 0);
            profileData.put("createdAt", updatedMidwife.getCreatedAt());
            profileData.put("updatedAt", updatedMidwife.getUpdatedAt());

            logger.info("Profile updated successfully for midwife: {}", updatedMidwife.getName());
            return ResponseEntity.ok(profileData);

        } catch (Exception e) {
            logger.error("Error updating midwife profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update midwife profile: " + e.getMessage());
        }
    }
}
