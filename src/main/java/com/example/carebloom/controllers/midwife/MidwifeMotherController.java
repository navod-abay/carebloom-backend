package com.example.carebloom.controllers.midwife;

import com.example.carebloom.models.Mother;
import com.example.carebloom.models.VitalRecord;
import com.example.carebloom.services.midwife.VitalRecordService;
import com.example.carebloom.repositories.MotherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/midwife/mothers")
@CrossOrigin(origins = "${app.cors.midwife-origin}")
public class MidwifeMotherController {

    @Autowired
    private MotherRepository motherRepository;

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

        // Get recent visits/vital records
        List<VitalRecord> recentVisits = vitalRecordService.getVisitRecordsByMotherId(motherId);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("mother", mother);
        response.put("recentVisits", recentVisits);
        
        // You can add healthDetails here if you have a HealthDetails model
        // response.put("healthDetails", healthDetailsService.getByMotherId(motherId));

        return ResponseEntity.ok(response);
    }

    /**
     * Get mother health details (basic info without visits)
     */
    @GetMapping("/{motherId}/health-details")
    public ResponseEntity<Mother> getMotherHealthDetails(@PathVariable String motherId) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mother not found"));
        return ResponseEntity.ok(mother);
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
}
