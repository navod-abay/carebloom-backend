package com.example.carebloom.repositories;

import com.example.carebloom.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    // Find all orders for a specific vendor
    @Query("{'vendor_id': ?0}")
    List<Order> findByVendorId(String vendorId);

    // Find orders by vendor and date range
    @Query("{'vendor_id': ?0, 'created_at': {$gte: ?1, $lt: ?2}}")
    List<Order> findByVendorIdAndCreatedAtBetween(String vendorId, LocalDateTime start, LocalDateTime end);

    // Find orders by vendor and status
    @Query("{'vendor_id': ?0, 'status': ?1}")
    List<Order> findByVendorIdAndStatus(String vendorId, String status);

    // Find orders by vendor, status and date range
    @Query("{'vendor_id': ?0, 'status': ?1, 'created_at': {$gte: ?2, $lt: ?3}}")
    List<Order> findByVendorIdAndStatusAndCreatedAtBetween(String vendorId, String status, LocalDateTime start,
            LocalDateTime end);

    // Count orders by vendor and date range
    @Query(value = "{'vendor_id': ?0, 'created_at': {$gte: ?1, $lt: ?2}}", count = true)
    long countByVendorIdAndCreatedAtBetween(String vendorId, LocalDateTime start, LocalDateTime end);

    // Count distinct customers by vendor and date range
    @Query("{'vendor_id': ?0, 'created_at': {$gte: ?1, $lt: ?2}}")
    List<Order> findOrdersForDistinctCustomerCount(String vendorId, LocalDateTime start, LocalDateTime end);

    // Find orders sorted by creation date
    @Query("{'vendor_id': ?0}")
    List<Order> findByVendorIdOrderByCreatedAtDesc(String vendorId);
}