package com.example.carebloom.controllers.midwife;

import com.example.carebloom.models.VitalRecord;
import com.example.carebloom.services.midwife.VitalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/midwife")
@CrossOrigin(origins = "${app.cors.midwife-origin}")
public class VitalRecordController {
    @Autowired
    private VitalRecordService vitalRecordService;

    /**
     * Create a new visit/vital record
     */
    @PostMapping("/visits")
    public ResponseEntity<VitalRecord> createVisitRecord(@RequestBody VitalRecord record) {
        VitalRecord savedRecord = vitalRecordService.saveVisitRecord(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
    }

    /**
     * Update an existing visit/vital record
     */
    @PutMapping("/visits/{visitId}")
    public ResponseEntity<VitalRecord> updateVisitRecord(
            @PathVariable String visitId,
            @RequestBody VitalRecord record) {
        record.setId(visitId);
        VitalRecord updatedRecord = vitalRecordService.updateVisitRecord(record);
        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * Get all visit records for a mother
     */
    @GetMapping("/mothers/{motherId}/visits")
    public ResponseEntity<List<VitalRecord>> getVisitRecordsByMotherId(@PathVariable String motherId) {
        List<VitalRecord> records = vitalRecordService.getVisitRecordsByMotherId(motherId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get the latest visit record for a mother
     */
    @GetMapping("/mothers/{motherId}/visits/latest")
    public ResponseEntity<VitalRecord> getLatestVisit(@PathVariable String motherId) {
        List<VitalRecord> records = vitalRecordService.getVisitRecordsByMotherId(motherId);
        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No visits found for this mother");
        }
        // Return the first record (assuming they're ordered by date)
        return ResponseEntity.ok(records.get(0));
    }

    /**
     * Get a specific visit record by ID
     */
    @GetMapping("/visits/{visitId}")
    public ResponseEntity<VitalRecord> getVisitRecordById(@PathVariable String visitId) {
        // You'll need to add this method to VitalRecordService
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not yet implemented");
    }
}
