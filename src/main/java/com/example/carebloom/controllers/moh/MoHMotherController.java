package com.example.carebloom.controllers.moh;

import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.repositories.MoHOfficeUserRepository;
import com.example.carebloom.repositories.MOHOfficeRepository;
import com.example.carebloom.repositories.ChildRepository;
import com.example.carebloom.repositories.WorkshopRepository;
import com.example.carebloom.dto.mother.MotherDetailsDto;
import com.example.carebloom.models.Child;
import com.example.carebloom.models.Workshop;
import com.example.carebloom.services.moh.MoHMotherService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MoHMotherController {

    private static final Logger logger = LoggerFactory.getLogger(MoHMotherController.class);

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private MoHOfficeUserRepository mohOfficeUserRepository;

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private WorkshopRepository workshopRepository;

    @Autowired
    private MoHMotherService mohMotherService;

    /**
     * Retrieves all mothers registered with a specific MOH office
     * 
     * @param mohOfficeId    The ID of the MOH office
     * @param authentication The authenticated user
     * @return List of mothers associated with the MOH office
     */
    @GetMapping("/mothers/{mohOfficeId}")
    public ResponseEntity<?> getMothersByOffice(@PathVariable String mohOfficeId, Authentication authentication) {
        try {
            // Verify user has access to this MOH office
            String firebaseUid = authentication.getName();
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);

            if (mohUser == null) {
                logger.error("No MOH user found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: User not found");
            }

            // Check if the authenticated user belongs to the requested MOH office
            if (!mohUser.getOfficeId().equals(mohOfficeId)) {
                logger.error("MOH user {} attempted to access mothers from another office: {}",
                        mohUser.getEmail(), mohOfficeId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: You can only access mothers from your own MOH office");
            }

            // Verify the MOH office exists
            if (!mohOfficeRepository.existsById(mohOfficeId)) {
                logger.error("Attempted to access mothers for non-existent MOH office: {}", mohOfficeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("MOH Office not found");
            }

            // Retrieve mothers for this MOH office
            List<Mother> mothers = motherRepository.findByMohOfficeId(mohOfficeId);
            logger.info("Retrieved {} mothers for MOH office: {}", mothers.size(), mohOfficeId);

            return ResponseEntity.ok(mothers);

        } catch (Exception e) {
            logger.error("Error retrieving mothers for MOH office: {}", mohOfficeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve mothers: " + e.getMessage()));
        }
    }

    /**
     * Retrieves detailed information about a specific mother
     * 
     * @param motherId       The ID of the mother
     * @param authentication The authenticated user
     * @return Detailed information about the mother including child records and
     *         workshops
     */
    @GetMapping("/mothers/details/{motherId}")
    public ResponseEntity<?> getMotherDetails(@PathVariable String motherId, Authentication authentication) {
        try {
            // Verify user is authenticated
            String firebaseUid = authentication.getName();
            MoHOfficeUser mohUser = mohOfficeUserRepository.findByFirebaseUid(firebaseUid);

            if (mohUser == null) {
                logger.error("No MOH user found for authenticated Firebase UID: {}", firebaseUid);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: User not found");
            }

            // Get mother by ID
            Optional<Mother> motherOpt = motherRepository.findById(motherId);
            if (!motherOpt.isPresent()) {
                logger.error("Mother not found with ID: {}", motherId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mother not found");
            }

            Mother mother = motherOpt.get();

            // Check if the authenticated user belongs to the same MOH office as the mother
            if (!mohUser.getOfficeId().equals(mother.getMohOfficeId())) {
                logger.error("MOH user {} attempted to access mother from another office: {}",
                        mohUser.getEmail(), mother.getMohOfficeId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: You can only access mothers from your own MOH office");
            }

            // Get child records for this mother
            List<Child> children = childRepository.findByMotherId(motherId);

            // Get workshops - assuming there might be a relation between workshops and
            // mothers
            // This is a placeholder implementation since the exact relationship is not
            // clear
            List<Workshop> workshops = workshopRepository.findByIsActiveTrueAndMohOfficeId(mother.getMohOfficeId());

            // Build the response DTO
            MotherDetailsDto detailsDto = new MotherDetailsDto();
            detailsDto.setId(mother.getId());
            detailsDto.setName(mother.getName());
            detailsDto.setRegistrationStatus(mother.getRegistrationStatus());
            detailsDto.setFirebaseUid(mother.getFirebaseUid());
            detailsDto.setEmail(mother.getEmail());
            detailsDto.setPhone(mother.getPhone());
            detailsDto.setDueDate(mother.getDueDate());
            detailsDto.setAddress(mother.getAddress());
            detailsDto.setDistrict(mother.getDistrict());
            detailsDto.setMohOfficeId(mother.getMohOfficeId());
            detailsDto.setRecordNumber(mother.getRecordNumber());

            // Set health records
            MotherDetailsDto.HealthRecordsDto healthRecords = new MotherDetailsDto.HealthRecordsDto();
            // Since these fields don't exist in the Mother model yet, we'll provide
            // placeholder data
            healthRecords.setAge(""); // Placeholder
            healthRecords.setBloodType(""); // Placeholder
            healthRecords.setMedicalHistory(""); // Placeholder
            healthRecords.setAllergies(""); // Placeholder
            healthRecords.setCurrentMedications(""); // Placeholder
            healthRecords.setEmergencyContact(""); // Placeholder
            detailsDto.setHealthRecords(healthRecords);

            // Set child records
            List<MotherDetailsDto.ChildRecordDto> childRecordDtos = children.stream()
                    .map(child -> {
                        MotherDetailsDto.ChildRecordDto dto = new MotherDetailsDto.ChildRecordDto();
                        dto.setId(child.getId());
                        dto.setName(child.getName());
                        dto.setDob(child.getDob());
                        dto.setGender(child.getGender());
                        dto.setBirthWeight(child.getBirthWeight());
                        dto.setBirthLength(child.getBirthLength());
                        dto.setVaccinations(child.getVaccinations());
                        dto.setHealthNotes(child.getHealthNotes());
                        return dto;
                    })
                    .collect(Collectors.toList());
            detailsDto.setChildRecords(childRecordDtos);

            // Set workshops
            List<MotherDetailsDto.WorkshopDto> workshopDtos = workshops.stream()
                    .map(workshop -> {
                        MotherDetailsDto.WorkshopDto dto = new MotherDetailsDto.WorkshopDto();
                        dto.setName(workshop.getTitle()); // Using title as name
                        dto.setDate(workshop.getDate());
                        return dto;
                    })
                    .collect(Collectors.toList());
            detailsDto.setWorkshops(workshopDtos);

            logger.info("Retrieved details for mother: {}", mother.getName());
            return ResponseEntity.ok(detailsDto);

        } catch (Exception e) {
            logger.error("Error retrieving mother details for ID: {}", motherId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve mother details: " + e.getMessage()));
        }
    }

    /**
     * Accept a mother's registration (change status from 'completed' to 'accepted')
     */
    @PostMapping("/mothers/{motherId}/accept")
    public ResponseEntity<?> acceptMotherRegistration(@PathVariable String motherId) {
        try {
            mohMotherService.acceptMotherRegistration(motherId);
            return ResponseEntity.ok(Map.of("message", "Mother registration accepted"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            logger.error("Error accepting mother registration for ID: {}", motherId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to accept mother registration: " + e.getMessage()));
        }
    }
}
