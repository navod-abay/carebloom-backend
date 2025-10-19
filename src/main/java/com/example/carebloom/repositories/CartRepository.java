package com.example.carebloom.repositories;

import com.example.carebloom.models.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<CartItem, String> {
    
    // Get all cart items for a user
    List<CartItem> findByUserId(String userId);
    
    // Find specific item in user's cart
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
    
    // Delete all items from user's cart
    void deleteByUserId(String userId);
    
    // Count items in user's cart
    long countByUserId(String userId);

    // Find cart items for a specific vendor
    List<CartItem> findByVendorId(String vendorId);
}