package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.hospitals.HospitalDashboardDto;
import com.example.carebloom.services.mother.MotherHospitalService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/mothers")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherHospitalController {

    private static final Logger logger = LoggerFactory.getLogger(MotherHospitalController.class);

    @Autowired
    private MotherHospitalService motherHospitalService;

    /**
     * Get hospital dashboard information for a specific mother
     * 
     * @param motherId The ID of the mother
     * @return HospitalDashboardDto containing hospital, midwife, visits, clinics,
     *         and workshops
     */
    @GetMapping("/moh")
    public ResponseEntity<HospitalDashboardDto> getHospitalDashboard() {
        try {
            HospitalDashboardDto dashboard = motherHospitalService
                    .getMotherHospitalDashboard();

            String responseJson = new ObjectMapper().writeValueAsString(dashboard);
            logger.info("Response size: {} characters", responseJson.length());
            logger.info("Successfully retrieved hospital dashboard");

            return ResponseEntity.ok(dashboard);

        } catch (RuntimeException e) {
            logger.error("Error retrieving hospital dashboard for mother ID : {}", e.getMessage());

            if (e.getMessage().contains("Mother not found")) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            logger.error("Unexpected error retrieving hospital dashboard for mother ID {}: {}",
                    e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
