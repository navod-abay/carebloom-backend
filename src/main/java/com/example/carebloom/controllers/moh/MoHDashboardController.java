package com.example.carebloom.controllers.moh;

import com.example.carebloom.dto.moh.DashboardResponse;
import com.example.carebloom.services.moh.MoHDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/moh")
@CrossOrigin(origins = "${app.cors.moh-origin}")
public class MoHDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(MoHDashboardController.class);

    @Autowired
    private MoHDashboardService dashboardService;

    /**
     * Get MOH dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                logger.debug("MOH user is not authenticated or anonymous");
                return ResponseEntity.status(401).body("User is not signed in properly");
            }

            String firebaseUid = authentication.getName();
            logger.debug("Getting dashboard data for MOH user with Firebase UID: {}", firebaseUid);

            DashboardResponse dashboard = dashboardService.getDashboardData(firebaseUid);
            
            logger.info("Dashboard data retrieved successfully for MOH user: {}", firebaseUid);
            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            logger.error("Error getting MOH dashboard data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to get dashboard data: " + e.getMessage());
        }
    }
}