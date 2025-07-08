package com.example.carebloom.controllers.admin;

import com.example.carebloom.services.admin.EmailCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/admin/users")
@CrossOrigin(origins = "${app.cors.admin-origin}")
public class EmailCheckController {

    private static final Logger logger = LoggerFactory.getLogger(EmailCheckController.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Autowired
    private EmailCheckService emailCheckService;

    /**
     * Check if an email is already registered in the system
     * 
     * @param email The email address to check
     * @return A JSON response indicating if the email exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        logger.debug("Received email check request for: {}", email);
        
        // Validate email format
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            response.put("error", "Invalid email format");
            response.put("message", "Please provide a valid email address");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean exists = emailCheckService.emailExists(email);
            
            if (exists) {
                response.put("exists", true);
                response.put("message", "Email already exists in the system");
            } else {
                response.put("exists", false);
                response.put("message", "Email is available");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking email: {}", e.getMessage());
            response.put("error", "Server error");
            response.put("message", "Unable to check email at this time");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
