package com.example.carebloom.controllers;

import com.example.carebloom.dto.DistrictDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for serving static district data.
 * This endpoint is publicly accessible for any client to access district information.
 */
@RestController
@RequestMapping("/api/v1/public/districts")
@CrossOrigin(origins = "*")
public class DistrictController {

    // Static list of districts in Sri Lanka
    private static final List<String> SRI_LANKA_DISTRICTS = Arrays.asList(
        "Ampara",
        "Anuradhapura",
        "Badulla",
        "Batticaloa",
        "Colombo",
        "Galle",
        "Gampaha",
        "Hambantota",
        "Jaffna",
        "Kalutara",
        "Kandy",
        "Kegalle",
        "Kilinochchi",
        "Kurunegala",
        "Mannar",
        "Matale",
        "Matara",
        "Monaragala",
        "Mullaitivu",
        "Nuwara Eliya",
        "Polonnaruwa",
        "Puttalam",
        "Ratnapura",
        "Trincomalee",
        "Vavuniya"
    );
    
    /**
     * Endpoint to get the list of all districts as DTOs
     */
    @GetMapping
    public ResponseEntity<List<DistrictDto>> getAllDistricts() {
        List<DistrictDto> districts = SRI_LANKA_DISTRICTS.stream()
            .map(name -> new DistrictDto(name))
            .collect(Collectors.toList());
        return ResponseEntity.ok(districts);
    }
    
    /**
     * Endpoint to get the list of all district names as simple strings
     */
    @GetMapping("/names")
    public ResponseEntity<List<String>> getDistrictNames() {
        return ResponseEntity.ok(SRI_LANKA_DISTRICTS);
    }
}
