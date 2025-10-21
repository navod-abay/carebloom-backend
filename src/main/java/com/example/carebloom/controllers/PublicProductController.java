package com.example.carebloom.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.carebloom.dto.product.ProductResponse;
import com.example.carebloom.services.PublicProductService;

@RestController
@RequestMapping("/api/v1/public/products")
@CrossOrigin(origins = "*")
public class PublicProductController {

    private static final Logger logger = LoggerFactory.getLogger(PublicProductController.class);

    @Autowired
    private PublicProductService publicProductService;

    /**
     * Get all active products from approved vendors for the mobile marketplace
     */
    @GetMapping
    public ResponseEntity<?> getAllPublicProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer sectionId) {
        try {
            logger.info("Fetching public products for mobile app - category: {}, sectionId: {}, search: {}", category, sectionId, search);

            List<ProductResponse> products;
            
            // Prioritize combined filters: category + sectionId + search
            if (search != null && !search.trim().isEmpty() && category != null && !category.trim().isEmpty() && sectionId != null) {
                products = publicProductService.getActiveProductsByCategorySectionAndName(category, sectionId, search.trim());
            } else if (search != null && !search.trim().isEmpty() && sectionId != null) {
                // search + section
                products = publicProductService.getActiveProductsBySectionIdAndName(sectionId, search.trim());
            } else if (category != null && !category.trim().isEmpty() && sectionId != null) {
                // category + section
                // fallback to category filter if specific combination not required
                products = publicProductService.getActiveProductsBySectionId(sectionId).stream()
                        .filter(p -> category.equals(p.getCategory()))
                        .collect(java.util.stream.Collectors.toList());
            } else if (search != null && !search.trim().isEmpty() && category != null && !category.trim().isEmpty()) {
                // existing name + category
                products = publicProductService.getActiveProductsByNameAndCategory(search.trim(), category);
            } else if (search != null && !search.trim().isEmpty()) {
                // Search by name only
                products = publicProductService.getActiveProductsByName(search.trim());
            } else if (sectionId != null) {
                // Filter by sectionId only
                products = publicProductService.getActiveProductsBySectionId(sectionId);
            } else if (category != null && !category.trim().isEmpty()) {
                // Filter by category only
                products = publicProductService.getActiveProductsByCategory(category);
            } else {
                // Get all active products from approved vendors
                products = publicProductService.getAllActiveProducts();
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", products,
                    "count", products.size(),
                    "message", "Public products retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching public products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch products: " + e.getMessage()));
        }
    }

    /**
     * Get products by vendor ID (for approved vendors only)
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<?> getProductsByVendor(@PathVariable String vendorId) {
        try {
            logger.info("Fetching products for vendor: {}", vendorId);

            List<ProductResponse> products = publicProductService.getActiveProductsByVendor(vendorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", products,
                    "count", products.size(),
                    "message", "Vendor products retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching products for vendor: {}", vendorId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch vendor products: " + e.getMessage()));
        }
    }

    /**
     * Get product by ID (for approved vendors only)
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable String productId) {
        try {
            logger.info("Fetching product details for ID: {}", productId);

            ProductResponse product = publicProductService.getActiveProductById(productId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", product,
                    "message", "Product retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching product: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch product: " + e.getMessage()));
        }
    }

    /**
     * Get all available categories from active products
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getAvailableCategories() {
        try {
            logger.info("Fetching available product categories");

            List<String> categories = publicProductService.getAvailableCategories();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", categories,
                    "count", categories.size(),
                    "message", "Categories retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch categories: " + e.getMessage()));
        }
    }

    /**
     * Get featured/best-selling products for mobile app home screen
     */
    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProducts(@RequestParam(defaultValue = "6") int limit) {
        try {
            logger.info("Fetching {} featured products for mobile app", limit);

            List<ProductResponse> products = publicProductService.getFeaturedProducts(limit);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", products,
                    "count", products.size(),
                    "message", "Featured products retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching featured products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch featured products: " + e.getMessage()));
        }
    }
}
