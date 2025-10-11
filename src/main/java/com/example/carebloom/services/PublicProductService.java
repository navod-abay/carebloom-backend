package com.example.carebloom.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.carebloom.dto.product.ProductResponse;
import com.example.carebloom.models.Product;
import com.example.carebloom.models.Vendor;
import com.example.carebloom.repositories.ProductRepository;
import com.example.carebloom.repositories.VendorRepository;

@Service
public class PublicProductService {

    private static final Logger logger = LoggerFactory.getLogger(PublicProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VendorRepository vendorRepository;

    /**
     * Get all active products from approved vendors
     */
    public List<ProductResponse> getAllActiveProducts() {
        logger.info("Fetching all active products from approved vendors");

        // Get all approved vendors
        List<Vendor> approvedVendors = vendorRepository.findByState("approved");
        List<String> approvedVendorIds = approvedVendors.stream()
                .map(Vendor::getId)
                .collect(Collectors.toList());

        // Get all active products from approved vendors
        List<Product> products = productRepository.findActiveProductsByVendorIds(approvedVendorIds);

        List<ProductResponse> productResponses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} active products from {} approved vendors", 
                   productResponses.size(), approvedVendors.size());
        return productResponses;
    }

    /**
     * Get active products by category from approved vendors
     */
    public List<ProductResponse> getActiveProductsByCategory(String category) {
        logger.info("Fetching active products by category: {}", category);

        // Get all approved vendors
        List<Vendor> approvedVendors = vendorRepository.findByState("approved");
        List<String> approvedVendorIds = approvedVendors.stream()
                .map(Vendor::getId)
                .collect(Collectors.toList());

        // Get products by category from approved vendors
        List<Product> products = productRepository.findActiveProductsByVendorIdsAndCategory(approvedVendorIds, category);

        List<ProductResponse> productResponses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} active products for category: {}", productResponses.size(), category);
        return productResponses;
    }

    /**
     * Search active products by name from approved vendors
     */
    public List<ProductResponse> getActiveProductsByName(String searchTerm) {
        logger.info("Searching active products by name: {}", searchTerm);

        // Get all approved vendors
        List<Vendor> approvedVendors = vendorRepository.findByState("approved");
        List<String> approvedVendorIds = approvedVendors.stream()
                .map(Vendor::getId)
                .collect(Collectors.toList());

        // Search products by name from approved vendors
        List<Product> products = productRepository.findActiveProductsByVendorIdsAndNameContaining(approvedVendorIds, searchTerm);

        List<ProductResponse> productResponses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} active products matching search term: {}", productResponses.size(), searchTerm);
        return productResponses;
    }

    /**
     * Search active products by name and category from approved vendors
     */
    public List<ProductResponse> getActiveProductsByNameAndCategory(String searchTerm, String category) {
        logger.info("Searching active products by name: {} and category: {}", searchTerm, category);

        // Get all approved vendors
        List<Vendor> approvedVendors = vendorRepository.findByState("approved");
        List<String> approvedVendorIds = approvedVendors.stream()
                .map(Vendor::getId)
                .collect(Collectors.toList());

        // Search products by name and category from approved vendors
        List<Product> products = productRepository.findActiveProductsByVendorIdsAndCategoryAndNameContaining(
                approvedVendorIds, category, searchTerm);

        List<ProductResponse> productResponses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} active products matching search term: {} and category: {}", 
                   productResponses.size(), searchTerm, category);
        return productResponses;
    }

    /**
     * Get active products by vendor ID (only if vendor is approved)
     */
    public List<ProductResponse> getActiveProductsByVendor(String vendorId) {
        logger.info("Fetching active products for vendor: {}", vendorId);

        // Check if vendor exists and is approved
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        if (!"approved".equals(vendor.getState())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vendor is not approved");
        }

        // Get active products for this vendor
        List<Product> products = productRepository.findByVendorIdAndStatusAndIsActiveTrue(vendorId, "active");

        List<ProductResponse> productResponses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("Found {} active products for vendor: {}", productResponses.size(), vendorId);
        return productResponses;
    }

    /**
     * Get active product by ID (only if vendor is approved)
     */
    public ProductResponse getActiveProductById(String productId) {
        logger.info("Fetching active product by ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Check if vendor is approved
        Vendor vendor = vendorRepository.findById(product.getVendorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        if (!"approved".equals(vendor.getState())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Product vendor is not approved");
        }

        // Check if product is active
        if (!product.getIsActive() || !"active".equals(product.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product is not active");
        }

        logger.info("Found active product: {}", product.getName());
        return mapToResponse(product);
    }

    /**
     * Get all available categories from active products of approved vendors
     */
    public List<String> getAvailableCategories() {
        logger.info("Fetching available categories from active products");

        // Get all approved vendors
        List<Vendor> approvedVendors = vendorRepository.findByState("approved");
        List<String> approvedVendorIds = approvedVendors.stream()
                .map(Vendor::getId)
                .collect(Collectors.toList());

        // Get distinct categories from active products of approved vendors
        List<String> categories = productRepository.findDistinctCategoriesByVendorIds(approvedVendorIds);

        logger.info("Found {} available categories", categories.size());
        return categories;
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setVendorId(product.getVendorId());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setStatus(product.getStatus());
        response.setDescription(product.getDescription());
        response.setImageUrl(product.getImageUrl());
        response.setSku(product.getSku());
        response.setWeight(product.getWeight());
        response.setDimensions(product.getDimensions());
        response.setLowStockThreshold(product.getLowStockThreshold());
        response.setIsActive(product.getIsActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
