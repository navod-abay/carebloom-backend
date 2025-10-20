package com.example.carebloom.controllers.midwife;

import com.example.carebloom.models.HealthDetails;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.VitalRecord;
import com.example.carebloom.services.midwife.VitalRecordService;
import com.example.carebloom.repositories.HealthDetailsRepository;
import com.example.carebloom.repositories.MotherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/midwife/mothers")
@CrossOrigin(origins = "${app.cors.midwife-origin}")
public class MidwifeMotherController {

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private HealthDetailsRepository healthDetailsRepository;

    @Autowired
    private VitalRecordService vitalRecordService;

    /**
     * Get mother health information including recent visits
     */
    @GetMapping("/{motherId}/health")
    public ResponseEntity<Map<String, Object>> getMotherHealthInfo(@PathVariable String motherId) {
        // Get mother details
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found"));

        // Get health details
        Optional<HealthDetails> healthDetails = healthDetailsRepository.findByMotherId(motherId);

        // Get recent visits/vital records
        List<VitalRecord> recentVisits = vitalRecordService.getVisitRecordsByMotherId(motherId);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("mother", mother);
        response.put("healthDetails", healthDetails.orElse(null));
        response.put("recentVisits", recentVisits);

        return ResponseEntity.ok(response);
    }

    /**
     * Get mother health details (basic info without visits)
     */
    @GetMapping("/{motherId}/health-details")
    public ResponseEntity<Map<String, Object>> getMotherHealthDetails(@PathVariable String motherId) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found"));

        Optional<HealthDetails> healthDetailsOpt = healthDetailsRepository.findByMotherId(motherId);

        Map<String, Object> response = new HashMap<>();
        response.put("mother", mother);

        if (healthDetailsOpt.isPresent()) {
            HealthDetails hd = healthDetailsOpt.get();
            System.out.println("DEBUG - Returning health details: " + hd);
            response.put("healthDetails", hd);
        } else {
            System.out.println("DEBUG - No health details found for mother: " + motherId);
            response.put("healthDetails", null);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all mothers assigned to this midwife
     */
    @GetMapping
    public ResponseEntity<List<Mother>> getAllMothers() {
        // TODO: Filter by midwife ID from authentication
        List<Mother> mothers = motherRepository.findAll();
        return ResponseEntity.ok(mothers);
    }

    /**
     * Get a specific mother by ID
     */
    @GetMapping("/{motherId}")
    public ResponseEntity<Mother> getMotherById(@PathVariable String motherId) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found"));
        return ResponseEntity.ok(mother);
    }

    /**
     * Create or update health details for a mother
     */
    @PostMapping("/{motherId}/health-details")
    public ResponseEntity<HealthDetails> createOrUpdateHealthDetails(
            @PathVariable String motherId,
            @RequestBody HealthDetails healthDetailsRequest) {

        // Verify mother exists
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found"));

        // Check if health details already exist
        Optional<HealthDetails> existingHealthDetails = healthDetailsRepository.findByMotherId(motherId);

        HealthDetails healthDetails;
        if (existingHealthDetails.isPresent()) {
            // Update existing record
            healthDetails = existingHealthDetails.get();
            healthDetails.setAge(healthDetailsRequest.getAge());
            healthDetails.setBloodType(healthDetailsRequest.getBloodType());
            healthDetails.setAllergies(healthDetailsRequest.getAllergies());
            healthDetails.setEmergencyContactName(healthDetailsRequest.getEmergencyContactName());
            healthDetails.setEmergencyContactPhone(healthDetailsRequest.getEmergencyContactPhone());
            healthDetails.setPregnancyType(healthDetailsRequest.getPregnancyType());
            healthDetails.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new record
            healthDetails = HealthDetails.builder()
                    .motherId(motherId)
                    .age(healthDetailsRequest.getAge())
                    .bloodType(healthDetailsRequest.getBloodType())
                    .allergies(healthDetailsRequest.getAllergies())
                    .emergencyContactName(healthDetailsRequest.getEmergencyContactName())
                    .emergencyContactPhone(healthDetailsRequest.getEmergencyContactPhone())
                    .pregnancyType(healthDetailsRequest.getPregnancyType())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        healthDetails = healthDetailsRepository.save(healthDetails);
        return ResponseEntity.ok(healthDetails);
    }

    /**
     * Update health details for a mother (PUT endpoint)
     */
    @PutMapping("/{motherId}/health-details")
    public ResponseEntity<HealthDetails> updateHealthDetails(
            @PathVariable String motherId,
            @RequestBody HealthDetails healthDetailsRequest) {

        // Verify mother exists
        motherRepository.findById(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found"));

        // Find existing health details
        HealthDetails healthDetails = healthDetailsRepository.findByMotherId(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Health details not found for this mother. Use POST to create."));

        // Update fields
        healthDetails.setAge(healthDetailsRequest.getAge());
        healthDetails.setBloodType(healthDetailsRequest.getBloodType());
        healthDetails.setAllergies(healthDetailsRequest.getAllergies());
        healthDetails.setEmergencyContactName(healthDetailsRequest.getEmergencyContactName());
        healthDetails.setEmergencyContactPhone(healthDetailsRequest.getEmergencyContactPhone());
        healthDetails.setPregnancyType(healthDetailsRequest.getPregnancyType());
        healthDetails.setUpdatedAt(LocalDateTime.now());

        healthDetails = healthDetailsRepository.save(healthDetails);
        return ResponseEntity.ok(healthDetails);
    }
}
