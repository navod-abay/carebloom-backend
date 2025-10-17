package com.example.carebloom.controllers.mother;

import com.example.carebloom.services.TextClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class InferenceController {

    private final TextClassificationService textClassificationService;

    @Autowired
    public InferenceController(TextClassificationService textClassificationService) {
        this.textClassificationService = textClassificationService;
    }

    @PostMapping("/classify")
    public PredictionResponse classifyText(@RequestBody PredictionRequest request) {
        String input = request.getText();
        String result = textClassificationService.predict(input);
        
        return new PredictionResponse(input, result);
    }
}

// Simple Request/Response DTOs
class PredictionRequest {
    private String text;
    // Getters and Setters omitted for brevity
    // ...
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}

class PredictionResponse {
    private String input;
    private String predictedLabel;
    
    public PredictionResponse(String input, String predictedLabel) {
        this.input = input;
        this.predictedLabel = predictedLabel;
    }
    // Getters omitted for brevity
    // ...
    public String getInput() { return input; }
    public String getPredictedLabel() { return predictedLabel; }
}