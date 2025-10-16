package com.example.carebloom.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Replace with your real AI endpoint
    private static final String AI_API_URL = "http://<ai-model-server>/classify";

    public String classifyQuestion(String text) {
        try {
            // Prepare request body
            Map<String, String> requestBody = Map.of("text", text);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Call the AI model
            ResponseEntity<Boolean> response = restTemplate.postForEntity(
                    AI_API_URL,
                    entity,
                    Boolean.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                boolean isMedical = response.getBody();
                return isMedical ? "medical" : "non-medical";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback if AI fails
        return "non-medical";
    }
}
