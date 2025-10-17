package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.mother.ConfirmFieldVisitTimeRequest;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.Mother.FieldVisitAppointment;
import com.example.carebloom.services.mother.MotherFieldVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/mothers")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherFieldVisitController {

    private static final Logger logger = LoggerFactory.getLogger(MotherFieldVisitController.class);

    @Autowired
    private MotherFieldVisitService motherFieldVisitService;

    /**
     * Confirm field visit time and update appointment status
     * 
     * @param request The confirmation request with available start and end times
     * @return Updated mother entity with confirmed field visit appointment
     */
    @PutMapping("/field-visit/confirm")
    public ResponseEntity<FieldVisitAppointment> confirmFieldVisitTime(@RequestBody ConfirmFieldVisitTimeRequest request) {
        try {
            Mother updatedMother = motherFieldVisitService.confirmFieldVisitTime(request);
            logger.info("Field visit time confirmed successfully");
            return ResponseEntity.ok(updatedMother.getFieldVisitAppointment());
        } catch (ResponseStatusException e) {
            logger.error("Error confirming field visit time: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            logger.error("Unexpected error confirming field visit time: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reschedule field visit by updating status to rescheduled
     * 
     * @return Updated mother entity with rescheduled field visit appointment
     */
    @PutMapping("/field-visit/reschedule")
    public ResponseEntity<Mother> rescheduleFieldVisit() {
        try {
            Mother updatedMother = motherFieldVisitService.rescheduleFieldVisit();
            logger.info("Field visit rescheduled successfully");
            return ResponseEntity.ok(updatedMother);
        } catch (ResponseStatusException e) {
            logger.error("Error rescheduling field visit: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            logger.error("Unexpected error rescheduling field visit: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
