package com.example.carebloom.controllers.vendor;

import com.example.carebloom.dto.product.CreateProductRequest;
import com.example.carebloom.dto.product.ProductResponse;
import com.example.carebloom.dto.product.UpdateProductRequest;
import com.example.carebloom.services.vendor.VendorProductService;
import com.example.carebloom.config.CustomAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vendor/products")
@CrossOrigin(origins = "${app.cors.vendor-origin}")
public class VendorProductController {

    private static final Logger logger = LoggerFactory.getLogger(VendorProductController.class);

    @Autowired
    private VendorProductService productService;

    /**
     * Get all products for the authenticated vendor
     */
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            Authentication authentication,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Fetching products for vendor: {}", vendorId);

            List<ProductResponse> products;
            
            if (search != null && !search.trim().isEmpty() && category != null && !category.trim().isEmpty()) {
                // Search by name and category
                products = productService.searchProductsByNameAndCategory(vendorId, search.trim(), category);
            } else if (search != null && !search.trim().isEmpty()) {
                // Search by name only
                products = productService.searchProductsByName(vendorId, search.trim());
            } else if (category != null && !category.trim().isEmpty()) {
                // Filter by category only
                products = productService.getProductsByCategory(vendorId, category);
            } else {
                // Get all products
                products = productService.getAllProductsForVendor(vendorId);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", products,
                    "count", products.size(),
                    "message", "Products retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch products: " + e.getMessage()));
        }
    }

    /**
     * Get product statistics for the authenticated vendor
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getProductStats(Authentication authentication) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Fetching product statistics for vendor: {}", vendorId);

            VendorProductService.ProductStatsResponse stats = productService.getProductStats(vendorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", stats,
                    "message", "Product statistics retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching product statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch product statistics: " + e.getMessage()));
        }
    }

    /**
     * Get active products for the authenticated vendor
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveProducts(Authentication authentication) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Fetching active products for vendor: {}", vendorId);

            List<ProductResponse> products = productService.getActiveProductsForVendor(vendorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", products,
                    "count", products.size(),
                    "message", "Active products retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching active products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch active products: " + e.getMessage()));
        }
    }

    /**
     * Get low stock products for the authenticated vendor
     */
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockProducts(Authentication authentication) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Fetching low stock products for vendor: {}", vendorId);

            List<ProductResponse> products = productService.getLowStockProducts(vendorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", products,
                    "count", products.size(),
                    "message", "Low stock products retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching low stock products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch low stock products: " + e.getMessage()));
        }
    }

    /**
     * Get a specific product by ID
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @PathVariable String productId,
            Authentication authentication) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Fetching product {} for vendor: {}", productId, vendorId);

            ProductResponse product = productService.getProductById(vendorId, productId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", product,
                    "message", "Product retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error fetching product with ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()));
        }
    }

    /**
     * Create a new product
     */
    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Creating new product for vendor: {}", vendorId);

            ProductResponse product = productService.createProduct(vendorId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "data", product,
                            "message", "Product created successfully"));
        } catch (ResponseStatusException e) {
            logger.error("Validation error creating product", e);
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "success", false,
                    "error", e.getReason()
            ));
        } catch (Exception e) {
            logger.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to create product: " + e.getMessage()));
        }
    }

    /**
     * Update an existing product
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request,
            Authentication authentication) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Updating product {} for vendor: {}", productId, vendorId);

            ProductResponse product = productService.updateProduct(vendorId, productId, request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", product,
                    "message", "Product updated successfully"));
        } catch (ResponseStatusException e) {
            logger.error("Validation error updating product {}", productId, e);
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "success", false,
                    "error", e.getReason()
            ));
        } catch (Exception e) {
            logger.error("Error updating product with ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()));
        }
    }

    /**
     * Delete a product
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable String productId,
            Authentication authentication) {
        try {
            CustomAuthenticationToken authToken = (CustomAuthenticationToken) authentication;
            String vendorId = authToken.getUserId();
            logger.info("Deleting product {} for vendor: {}", productId, vendorId);

            productService.deleteProduct(vendorId, productId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Product deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting product with ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()));
        }
    }
}
