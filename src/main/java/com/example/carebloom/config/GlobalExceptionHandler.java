package com.example.carebloom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException e) {
        logger.error("ResponseStatusException: {} - {}", e.getStatusCode(), e.getReason(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", e.getReason());
        response.put("status", e.getStatusCode().value());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(e.getStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", "An unexpected error occurred");
        response.put("status", 500);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(500).body(response);
    }
}
