package com.example.carebloom.dto.mother;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to confirm photo upload completion")
public class PhotoConfirmRequest {
    
    @Schema(description = "Original file name", example = "profile_image.jpg")
    @NotBlank(message = "File name is required")
    private String fileName;
}