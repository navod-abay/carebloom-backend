package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.unit.UnitBasicDTO;
import com.example.carebloom.services.mother.MotherUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mothers")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherUnitController {

    private static final Logger logger = LoggerFactory.getLogger(MotherUnitController.class);

    @Autowired
    private MotherUnitService motherUnitService;

    /**
     * Get all units assigned to a specific midwife
     * 
     * @param midwifeId The ID of the midwife
     * @return List of UnitBasicDTO assigned to the midwife
     */
    @GetMapping("/units/midwife/{midwifeId}")
    public ResponseEntity<?> getUnitsByMidwife(@PathVariable String midwifeId) {
        try {
            logger.debug("Getting units for midwife with ID: {}", midwifeId);

            List<UnitBasicDTO> units = motherUnitService.getUnitsByMidwifeId(midwifeId);
            
            logger.info("Retrieved {} units for midwife: {}", units.size(), midwifeId);
            return ResponseEntity.ok(units);

        } catch (Exception e) {
            logger.error("Error getting units for midwife: {}", midwifeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to get units for midwife: " + e.getMessage());
        }
    }
}
