package com.example.carebloom.repositories;

import com.example.carebloom.models.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // Find cart items by vendor and date range
    @Query("{'vendorId': ?0, 'addedAt': {$gte: ?1, $lt: ?2}}")
    List<CartItem> findByVendorIdAndAddedAtBetween(String vendorId, LocalDateTime start, LocalDateTime end);

    // Find cart items by vendor, order status and date range
    @Query("{'vendorId': ?0, 'orderStatus': ?1, 'addedAt': {$gte: ?2, $lt: ?3}}")
    List<CartItem> findByVendorIdAndOrderStatusAndAddedAtBetween(String vendorId, String orderStatus,
            LocalDateTime start, LocalDateTime end);

    // Find delivered/sold items by vendor and date range
    @Query("{'vendorId': ?0, 'orderStatus': 'delivered', 'deliveredAt': {$gte: ?1, $lt: ?2}}")
    List<CartItem> findSoldItemsByVendorIdAndDeliveredAtBetween(String vendorId, LocalDateTime start,
            LocalDateTime end);

    // Count distinct customers by vendor and date range
    @Query("{'vendorId': ?0, 'addedAt': {$gte: ?1, $lt: ?2}}")
    List<CartItem> findCartItemsForDistinctCustomerCount(String vendorId, LocalDateTime start, LocalDateTime end);
}