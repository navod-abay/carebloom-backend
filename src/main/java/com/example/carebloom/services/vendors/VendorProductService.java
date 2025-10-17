package com.example.carebloom.services.vendors;

import com.example.carebloom.config.CategorySectionRegistry;
import com.example.carebloom.dto.product.CreateProductRequest;
import com.example.carebloom.dto.product.ProductResponse;
import com.example.carebloom.dto.product.UpdateProductRequest;
import com.example.carebloom.models.Product;
import com.example.carebloom.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service("vendorsProductService")
public class VendorProductService {

    private static final Logger logger = LoggerFactory.getLogger(VendorProductService.class);

    @Autowired
    private ProductRepository productRepository;

    /**
     * Get all products for a vendor
     */
    public List<ProductResponse> getAllProductsForVendor(String vendorId) {
        logger.info("Fetching all products for vendor: {}", vendorId);

        List<Product> products = productRepository.findByVendorId(vendorId);

        logger.info("Found {} products for vendor: {}", products.size(), vendorId);
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active products for a vendor
     */
    public List<ProductResponse> getActiveProductsForVendor(String vendorId) {
        logger.info("Fetching active products for vendor: {}", vendorId);

        List<Product> products = productRepository.findByVendorIdAndIsActiveTrue(vendorId);

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category for a vendor
     */
    public List<ProductResponse> getProductsByCategory(String vendorId, String category) {
        logger.info("Fetching products by category '{}' for vendor: {}", category, vendorId);

        List<Product> products = productRepository.findByVendorIdAndCategory(vendorId, category);

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search products by name
     */
    public List<ProductResponse> searchProductsByName(String vendorId, String searchTerm) {
        logger.info("Searching products with term '{}' for vendor: {}", searchTerm, vendorId);

        List<Product> products = productRepository.findByVendorIdAndNameContainingIgnoreCase(vendorId, searchTerm);

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search products by name and category
     */
    public List<ProductResponse> searchProductsByNameAndCategory(String vendorId, String searchTerm, String category) {
        logger.info("Searching products with term '{}' and category '{}' for vendor: {}", searchTerm, category,
                vendorId);

        List<Product> products = productRepository.findByVendorIdAndCategoryAndNameContainingIgnoreCase(vendorId,
                category, searchTerm);

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock products
     */
    public List<ProductResponse> getLowStockProducts(String vendorId) {
        logger.info("Fetching low stock products for vendor: {}", vendorId);

        List<Product> products = productRepository.findByVendorIdAndStockLessThanEqual(vendorId, 10);

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific product by ID
     */
    public ProductResponse getProductById(String vendorId, String productId) {
        logger.info("Fetching product {} for vendor: {}", productId, vendorId);

        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found with id: " + productId));

        return mapToResponse(product);
    }

    /**
     * Create a new product
     */
    public ProductResponse createProduct(String vendorId, CreateProductRequest request) {
        logger.info("Creating product for vendor: {}", vendorId);

        // Validate sectionId vs category mapping if provided
        if (request.getSectionId() != null
                && !CategorySectionRegistry.isSectionValidForCategory(request.getSectionId(), request.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "sectionId is not valid for the selected category");
        }

        Product product = new Product();
        product.setVendorId(vendorId);
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setSectionId(request.getSectionId());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setSku(request.getSku());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // Auto-update status based on stock
        if (product.getStock() == 0) {
            product.setStatus("out-of-stock");
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully with ID: {}", savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    /**
     * Update an existing product
     */
    public ProductResponse updateProduct(String vendorId, String productId, UpdateProductRequest request) {
        logger.info("Updating product {} for vendor: {}", productId, vendorId);

        Product product = productRepository.findByIdAndVendorId(productId, vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found with id: " + productId));

        // Update fields if provided
        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getCategory() != null)
            product.setCategory(request.getCategory());
        if (request.getSectionId() != null)
            product.setSectionId(request.getSectionId());
        if (request.getPrice() != null)
            product.setPrice(request.getPrice());
        if (request.getStock() != null) {
            product.setStock(request.getStock());
            // Auto-update status based on stock
            if (request.getStock() == 0) {
                product.setStatus("out-of-stock");
            } else if ("out-of-stock".equals(product.getStatus()) && request.getStock() > 0) {
                product.setStatus("active");
            }
        }
        if (request.getStatus() != null)
            product.setStatus(request.getStatus());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getImageUrl() != null)
            product.setImageUrl(request.getImageUrl());
        if (request.getSku() != null)
            product.setSku(request.getSku());
        if (request.getWeight() != null)
            product.setWeight(request.getWeight());
        if (request.getDimensions() != null)
            product.setDimensions(request.getDimensions());
        if (request.getLowStockThreshold() != null)
            product.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getIsActive() != null)
            product.setIsActive(request.getIsActive());

        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated successfully: {}", productId);

        return mapToResponse(updatedProduct);
    }

    /**
     * Delete a product
     */
    public void deleteProduct(String vendorId, String productId) {
        logger.info("Deleting product {} for vendor: {}", productId, vendorId);

        if (!productRepository.existsByIdAndVendorId(productId, vendorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Product not found with id: " + productId);
        }

        productRepository.deleteByIdAndVendorId(productId, vendorId);
        logger.info("Product deleted successfully: {}", productId);
    }

    /**
     * Get product statistics for vendor
     */
    public ProductStatsResponse getProductStats(String vendorId) {
        logger.info("Fetching product statistics for vendor: {}", vendorId);

        List<Product> allProducts = productRepository.findByVendorId(vendorId);

        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream().filter(p -> "active".equals(p.getStatus())).count();
        long outOfStockProducts = allProducts.stream().filter(p -> "out-of-stock".equals(p.getStatus())).count();
        long lowStockProducts = allProducts.stream()
                .filter(p -> {
                    int stock = p.getStock() != null ? p.getStock() : 0;
                    int threshold = p.getLowStockThreshold() != null ? p.getLowStockThreshold() : 10;
                    return stock <= threshold;
                })
                .count();

        return new ProductStatsResponse(totalProducts, activeProducts, outOfStockProducts, lowStockProducts);
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getVendorId(),
                product.getName(),
                product.getCategory(),
                product.getSectionId(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getDescription(),
                product.getImageUrl(),
                product.getSku(),
                product.getWeight(),
                product.getDimensions(),
                product.getLowStockThreshold(),
                product.getIsActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    /**
     * Inner class for product statistics
     */
    public static class ProductStatsResponse {
        private final long totalProducts;
        private final long activeProducts;
        private final long outOfStockProducts;
        private final long lowStockProducts;

        public ProductStatsResponse(long totalProducts, long activeProducts, long outOfStockProducts,
                long lowStockProducts) {
            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
            this.outOfStockProducts = outOfStockProducts;
            this.lowStockProducts = lowStockProducts;
        }

        // Getters
        public long getTotalProducts() {
            return totalProducts;
        }

        public long getActiveProducts() {
            return activeProducts;
        }

        public long getOutOfStockProducts() {
            return outOfStockProducts;
        }

        public long getLowStockProducts() {
            return lowStockProducts;
        }
    }
}
