package com.example.carebloom.dto.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    
    private String name;
    private String category;
    
    @Positive(message = "Price must be positive")
    private Double price;
    
    @PositiveOrZero(message = "Stock must be positive or zero")
    private Integer stock;
    
    private String status;
    private String description;
    private String imageUrl;
    private String sku;
    private Double weight;
    private String dimensions;
    private Integer lowStockThreshold;
    private Boolean isActive;
}
