package com.example.carebloom.controllers.mother;

import com.example.carebloom.dto.mother.DashboardResponse;
import com.example.carebloom.services.mother.MotherDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mothers")
@CrossOrigin(origins = "${app.cors.mother-origin}")
public class MotherDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(MotherDashboardController.class);

    @Autowired
    private MotherDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.debug("No authentication context available");
                return ResponseEntity.status(401).body("Unauthorized: No authentication context");
            }

            String firebaseUid = authentication.getName();
            if (firebaseUid == null || firebaseUid.isEmpty()) {
                logger.debug("No Firebase UID found in authentication context");
                return ResponseEntity.status(401).body("Unauthorized: No Firebase UID found");
            }

            logger.info("Fetching dashboard data for Firebase UID: {}", firebaseUid);
            DashboardResponse dashboardData = dashboardService.getDashboardData(firebaseUid);
            
            logger.info("Dashboard data successfully retrieved for Firebase UID: {}", firebaseUid);
            return ResponseEntity.ok(dashboardData);

        } catch (Exception e) {
            logger.error("Error retrieving dashboard data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to retrieve dashboard data: " + e.getMessage());
        }
    }
}
