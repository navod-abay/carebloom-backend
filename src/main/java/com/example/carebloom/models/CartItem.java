package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart_items")
public class CartItem {
    @Id
    private String id;
    
    private String userId; // Mother's Firebase UID
    private String productId; // Reference to Product
    private String vendorId; // Reference to vendor for easier queries
    private Integer quantity;
    private Double priceAtAdd; // Price when added to cart (for price changes)
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}