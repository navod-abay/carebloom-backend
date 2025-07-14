package com.example.carebloom.controllers;

import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.dto.MOHOfficeDto;
import com.example.carebloom.repositories.MOHOfficeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for accessing MOH office information by district.
 * This endpoint is publicly accessible for anyone to find MOH offices by district.
 */
@RestController
@RequestMapping("/api/v1/public/moh-offices")
@CrossOrigin(origins = "*")
public class DistrictMOHOfficeController {
    private static final Logger logger = LoggerFactory.getLogger(DistrictMOHOfficeController.class);

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;

    /**
     * Get all MOH offices in a specific district
     * 
     * @param district The district name
     * @return List of MOH offices in the specified district as DTOs
     */
    @GetMapping("/by-district/{district}")
    public ResponseEntity<?> getMOHOfficesByDistrict(@PathVariable String district) {
        try {
            logger.info("Fetching MOH offices for district: {}", district);
            List<MOHOffice> offices = mohOfficeRepository.findByDistrict(district);
            
            // Convert entities to DTOs
            List<MOHOfficeDto> officeDtos = offices.stream()
                .map(MOHOfficeDto::fromEntity)
                .collect(Collectors.toList());
            
            if (officeDtos.isEmpty()) {
                logger.info("No MOH offices found for district: {}", district);
                return ResponseEntity.ok(Map.of(
                    "message", "No MOH offices found in this district", 
                    "offices", officeDtos
                ));
            }
            
            return ResponseEntity.ok(officeDtos);
        } catch (Exception e) {
            logger.error("Error fetching MOH offices by district", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to retrieve MOH offices: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all MOH offices (potentially for admin purposes or for a district selector)
     * 
     * @return List of all MOH offices as DTOs
     */
    @GetMapping
    public ResponseEntity<?> getAllMOHOffices() {
        try {
            logger.info("Fetching all MOH offices");
            List<MOHOffice> offices = mohOfficeRepository.findAll();
            
            // Convert entities to DTOs
            List<MOHOfficeDto> officeDtos = offices.stream()
                .map(MOHOfficeDto::fromEntity)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(officeDtos);
        } catch (Exception e) {
            logger.error("Error fetching all MOH offices", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to retrieve MOH offices: " + e.getMessage()
            ));
        }
    }
}
