package com.example.carebloom.controllers.midwife;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.carebloom.dto.clinics.ClinicMidwifeDTO;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.services.ClinicService;
import com.example.carebloom.config.CustomAuthenticationToken;


@RestController
@CrossOrigin(origins = "${app.cors.midwife-origin}")
@RequestMapping("/api/v1/midwife/clinics")
public class ClinicController {

    private static final Logger logger = LoggerFactory.getLogger(ClinicController.class);
    
    @Autowired
    private ClinicService clinicService;

    @GetMapping()
    public ResponseEntity<?> getClinics() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            Midwife midwifeEntity = authToken.getUserEntity(Midwife.class);
            logger.info("Fetching clinics for midwife");

            List<ClinicMidwifeDTO> clinics = clinicService.getClinicsForMidwife(midwifeEntity.getOfficeId());
            return ResponseEntity.ok(clinics);
        } catch (Exception e) {
            logger.error("Error fetching clinics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch clinics");
        }
    }
}
