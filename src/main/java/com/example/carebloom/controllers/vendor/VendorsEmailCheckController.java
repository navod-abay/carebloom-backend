package com.example.carebloom.controllers.vendor;

import com.example.carebloom.services.vendors.VendorsEmailCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/vendor/email-check")
public class VendorsEmailCheckController {

    private static final Logger logger = LoggerFactory.getLogger(VendorsEmailCheckController.class);

    @Autowired
    @Qualifier("vendorsEmailCheckService")
    private VendorsEmailCheckService emailCheckService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @GetMapping
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        logger.info("Checking email: {}", email);

        HashMap<String, Object> response = new HashMap<>();

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            response.put("error", true);
            response.put("message", "Invalid email format");
            return ResponseEntity.badRequest().body(response);
        }

        boolean exists = emailCheckService.emailExists(email);
        response.put("exists", exists);
        response.put("error", false);
        return ResponseEntity.ok(response);
    }
}
