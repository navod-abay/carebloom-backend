package com.example.carebloom.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.carebloom.models.Product;

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

    // Find products by vendor ID and sectionId (sub-category)
    List<Product> findByVendorIdAndSectionId(String vendorId, Integer sectionId);
    
    // Find products by vendor ID with name containing (case insensitive)
    List<Product> findByVendorIdAndNameContainingIgnoreCase(String vendorId, String name);
    
    // Find products by vendor ID and category with name containing
    List<Product> findByVendorIdAndCategoryAndNameContainingIgnoreCase(String vendorId, String category, String name);
    
    // Find products by vendor ID and sectionId with name containing
    List<Product> findByVendorIdAndSectionIdAndNameContainingIgnoreCase(String vendorId, Integer sectionId, String name);
    
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
    
    // Public API methods for mobile app - find active products from approved vendors
    @Query("{ 'vendorId': { $in: ?0 }, 'isActive': true, 'status': 'active' }")
    List<Product> findActiveProductsByVendorIds(List<String> vendorIds);
    
    @Query("{ 'vendorId': { $in: ?0 }, 'category': ?1, 'isActive': true, 'status': 'active' }")
    List<Product> findActiveProductsByVendorIdsAndCategory(List<String> vendorIds, String category);
    
    @Query("{ 'vendorId': { $in: ?0 }, 'sectionId': ?1, 'isActive': true, 'status': 'active' }")
    List<Product> findActiveProductsByVendorIdsAndSectionId(List<String> vendorIds, Integer sectionId);
    
    @Query("{ 'vendorId': { $in: ?0 }, 'name': { $regex: ?1, $options: 'i' }, 'isActive': true, 'status': 'active' }")
    List<Product> findActiveProductsByVendorIdsAndNameContaining(List<String> vendorIds, String searchTerm);
    
    @Query("{ 'vendorId': { $in: ?0 }, 'sectionId': ?1, 'name': { $regex: ?2, $options: 'i' }, 'isActive': true, 'status': 'active' }")
    List<Product> findActiveProductsByVendorIdsAndSectionIdAndNameContaining(List<String> vendorIds, Integer sectionId, String searchTerm);
    
    @Query("{ 'vendorId': { $in: ?0 }, 'category': ?1, 'sectionId': ?2, 'name': { $regex: ?3, $options: 'i' }, 'isActive': true, 'status': 'active' }")
    List<Product> findActiveProductsByVendorIdsAndCategoryAndSectionIdAndNameContaining(List<String> vendorIds, String category, Integer sectionId, String searchTerm);
    
    @Query("{ 'vendorId': { $in: ?0 }, 'category': ?1, 'name': { $regex: ?2, $options: 'i' }, 'isActive': true, 'status': 'active' }")
    List<Product> findActiveProductsByVendorIdsAndCategoryAndNameContaining(List<String> vendorIds, String category, String searchTerm);
    
    // Find products by vendor ID, status and active status
    List<Product> findByVendorIdAndStatusAndIsActiveTrue(String vendorId, String status);
    
    // Get distinct categories from active products of approved vendors
    @Query(value = "{ 'vendorId': { $in: ?0 }, 'isActive': true, 'status': 'active' }", fields = "{ 'category': 1 }")
    List<Product> findCategoriesByVendorIds(List<String> vendorIds);
    
    // Helper method to get distinct categories
    default List<String> findDistinctCategoriesByVendorIds(List<String> vendorIds) {
        return findCategoriesByVendorIds(vendorIds).stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
}
