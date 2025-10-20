package com.example.carebloom.controllers.mothers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/moh")
public class ChildRecordController {

    @PostMapping("/child-records")
    public ResponseEntity<?> addChildRecord(@RequestBody Map<String, Object> childData) {
        try {
            System.out.println("📥 Received new child record: " + childData);

            // TODO: In the future, save to database here

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Child record added successfully");
            response.put("data", childData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
