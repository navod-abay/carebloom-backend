package com.example.carebloom.services;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.huggingface.translator.TextClassificationTranslator;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.Classifications;
import ai.djl.ModelException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TextClassificationService {

    private Predictor<String, Classifications> predictor;
    private ZooModel<String, Classifications> model;

    private static final Logger logger = LoggerFactory.getLogger(TextClassificationService.class);

    // This should point to the directory containing your model files (config.json, pytorch_model.bin, etc.)
    // IMPORTANT: Update this path to where your model artifacts are stored.
    private static final String LOCAL_MODEL_ROOT_PATH = "biobert-model"; 
    // This should be the file prefix of your model weights, typically 'pytorch_model' or the directory name.
    private static final String MODEL_FILE_PREFIX = "traced_model_for_djl.pt"; 

    @PostConstruct
    private void loadModel() {
        try {
            logger.info("=== Starting AI Model Loading Process ===");
            Path modelPath = Paths.get(LOCAL_MODEL_ROOT_PATH);
            logger.info("Model path: {}", modelPath.toAbsolutePath());
            logger.info("Model file prefix: {}", MODEL_FILE_PREFIX);
            
            // Check if model directory exists
            if (!modelPath.toFile().exists()) {
                logger.error("Model directory does not exist: {}", modelPath.toAbsolutePath());
                logger.warn("AI model will not be available. Forum will use fallback classification.");
                return; // Don't throw exception, just return without loading model
            }
            
            // List files in model directory
            try {
                logger.info("Files in model directory:");
                modelPath.toFile().listFiles();
                for (var file : modelPath.toFile().listFiles()) {
                    logger.info("  - {}", file.getName());
                }
            } catch (Exception e) {
                logger.warn("Could not list model directory files: {}", e.getMessage());
            }

            // 1. Configure the Criteria builder.
            // The ModelNotFoundException is typically solved by ensuring:
            // A) The path (optModelPath) is correct.
            // B) The file prefix (optModelName) is correct relative to the path.
            // C) The correct engine and application are specified.
            
            logger.info("Building model criteria...");
            Criteria<String, Classifications> criteria = Criteria.builder()
                    // Define the Input (String) and Output (Classifications) types
                    .setTypes(String.class, Classifications.class) 
                    
                    // === A) Point to the Local Directory where config.json and weights reside ===
                    .optModelPath(modelPath) 
                    
                    // === B) Set the model file prefix relative to optModelPath ===
                    // If your model weights file is "pytorch_model.bin", set this to "pytorch_model"
                    .optModelName(MODEL_FILE_PREFIX) 
                    
                    // === C) Specify Engine and Application for HuggingFace Transformers ===
                    // Assuming it's a PyTorch model:
                    .optEngine("PyTorch") 
                    .optApplication(ai.djl.Application.NLP.TEXT_CLASSIFICATION)
                    
                    // Add HuggingFace translator
                    .optTranslator(TextClassificationTranslator.builder(
                        HuggingFaceTokenizer.newInstance(modelPath)
                    ).build())
                    
                    .build();

            logger.info("Model criteria built successfully");
            logger.info("Attempting to load model from path: {}", modelPath.toAbsolutePath());
            
            try {
                // This is the line where the error occurred (or immediately after)
                model = criteria.loadModel();
                logger.info("Model loaded successfully: {}", model.getName());
            } catch (Exception e) {
                logger.error("Failed to load model: {}", e.getMessage(), e);
                logger.warn("AI model will not be available. Forum will use fallback classification.");
                return; // Don't throw exception, just return without loading model
            }
            
            try {
                // Create predictor from the loaded model
                predictor = model.newPredictor();
                logger.info("Predictor created successfully");
            } catch (Exception e) {
                logger.error("Failed to create predictor: {}", e.getMessage(), e);
                logger.warn("AI model will not be available. Forum will use fallback classification.");
                return; // Don't throw exception, just return without loading model
            }
            
            logger.info("=== AI Model Loading Complete ===");
        } catch (Exception e) {
            logger.error("Unexpected error during model loading: {}", e.getMessage(), e);
            logger.warn("AI model will not be available. Forum will use fallback classification.");
        }
    }

    public String predict(String text) {
        logger.debug("=== Starting Text Classification ===");
        logger.debug("Input text length: {} characters", text != null ? text.length() : 0);
        logger.debug("Input text preview: '{}'", text != null ? text.substring(0, Math.min(100, text.length())) + "..." : "null");
        
        if (predictor == null) {
            logger.warn("Predictor is null - model not properly loaded. Using fallback classification.");
            // Return a fallback classification instead of throwing an exception
            return "MEDICAL"; // Default to medical for safety
        }
        
        try {
            logger.info("Making prediction with AI model...");
            // Predict returns Classifications object, extract the top classification
            Classifications result = predictor.predict(text);
            
            logger.info("Prediction completed. Number of classifications: {}", result.items().size());
            logger.info("Best classification: {} (probability: {})", 
                       result.best().getClassName(), result.best().getProbability());
            
            // Log all classifications for debugging
            int count = 0;
            for (var classification : result.items()) {
                if (count >= 3) break;
                logger.debug("Classification {}: {} (probability: {})", 
                           count, classification.getClassName(), classification.getProbability());
                count++;
            }
            
            String bestClass = result.best().getClassName();
            logger.info("Returning classification: '{}'", bestClass);
            return bestClass; 
            
        } catch (Exception e) {
            logger.error("Prediction failed: {}", e.getMessage(), e);
            return "MEDICAL"; // Default to medical for safety when prediction fails
        }
    }

    @PreDestroy
    public void close() {
        logger.info("Closing AI model resources...");
        if (predictor != null) {
            predictor.close();
            logger.info("Predictor closed");
        }
        if (model != null) {
            model.close();
            logger.info("Model closed");
        }
        logger.info("AI model cleanup complete");
    }
}