package com.example.carebloom.dto.cart;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String id;
    private String productId;
    private String productName;
    private String category;
    private String imageUrl;
    private Double currentPrice;
    private Double priceAtAdd;
    private Integer quantity;
    private Integer availableStock;
    private String vendorId;
    private String vendorName;
    private LocalDateTime addedAt;
    
    // Calculated fields
    private Double itemTotal; // priceAtAdd * quantity
    private Boolean priceChanged; // currentPrice != priceAtAdd
}