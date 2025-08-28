package com.example.carebloom.repositories;

import com.example.carebloom.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    // Find all products by vendor ID
    List<Product> findByVendorId(String vendorId);
    
    // Find active products by vendor ID
    List<Product> findByVendorIdAndIsActiveTrue(String vendorId);
    
    // Find products by vendor ID and status
    List<Product> findByVendorIdAndStatus(String vendorId, String status);
    
    // Find products by vendor ID and category
    List<Product> findByVendorIdAndCategory(String vendorId, String category);
    
    // Find products by vendor ID with name containing (case insensitive)
    List<Product> findByVendorIdAndNameContainingIgnoreCase(String vendorId, String name);
    
    // Find products by vendor ID and category with name containing
    List<Product> findByVendorIdAndCategoryAndNameContainingIgnoreCase(String vendorId, String category, String name);
    
    // Find low stock products by vendor ID
    List<Product> findByVendorIdAndStockLessThanEqual(String vendorId, Integer threshold);
    
    // Find products by vendor ID and stock greater than 0
    List<Product> findByVendorIdAndStockGreaterThan(String vendorId, Integer stock);
    
    // Find a specific product by vendor ID and product ID
    Optional<Product> findByIdAndVendorId(String id, String vendorId);
    
    // Check if product exists by vendor ID and product ID
    boolean existsByIdAndVendorId(String id, String vendorId);
    
    // Delete by vendor ID and product ID
    void deleteByIdAndVendorId(String id, String vendorId);
}
