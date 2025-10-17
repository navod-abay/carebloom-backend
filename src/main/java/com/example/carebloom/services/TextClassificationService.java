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
    private void loadModel() throws ModelException, IOException {
        Path modelPath = Paths.get(LOCAL_MODEL_ROOT_PATH);

        // 1. Configure the Criteria builder.
        // The ModelNotFoundException is typically solved by ensuring:
        // A) The path (optModelPath) is correct.
        // B) The file prefix (optModelName) is correct relative to the path.
        // C) The correct engine and application are specified.
        
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

        logger.info("Attempting to load model from path: {}", modelPath.toAbsolutePath());
        
        // This is the line where the error occurred (or immediately after)
        model = criteria.loadModel();
        
        // Create predictor from the loaded model
        predictor = model.newPredictor();
        
        logger.info("Model loaded successfully: {}", model.getName());
    }

    public String predict(String text) {
        if (predictor == null) {
            throw new IllegalStateException("Model not loaded.");
        }
        try {
            // Predict returns Classifications object, extract the top classification
            Classifications result = predictor.predict(text);
            return result.best().getClassName(); 
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Prediction failed.";
        }
    }

    @PreDestroy
    public void close() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
    }
}