package com.example.carebloom.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.carebloom.models.CartItem;
import com.example.carebloom.models.Product;
import com.example.carebloom.repositories.CartRepository;
import com.example.carebloom.repositories.ProductRepository;

/**
 * Test controller to add sample data for testing vendor orders integration
 */
@RestController
@RequestMapping("/api/v1/test")
@CrossOrigin(origins = "*")
public class TestDataController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/cart-count")
    public ResponseEntity<?> getCartCount() {
        long count = cartRepository.count();
        long vendorCount = cartRepository.findByVendorId("demo-vendor").size();
        return ResponseEntity.ok(new CountResponse(count, vendorCount));
    }

    @PostMapping("/add-sample-cart-data")
    public ResponseEntity<?> addSampleCartData() {
        // Find or create a test product for demo-vendor
        List<Product> demoProducts = productRepository.findByVendorId("demo-vendor");
        Product testProduct;
        
        if (demoProducts.isEmpty()) {
            // Create a test product
            testProduct = new Product();
            testProduct.setName("Test Product for Demo Vendor");
            testProduct.setDescription("A test product to verify cart integration");
            testProduct.setPrice(29.99);
            testProduct.setVendorId("demo-vendor");
            testProduct.setCategory("test");
            testProduct.setImageUrl("https://via.placeholder.com/200");
            testProduct.setSku("TEST-001");
            testProduct.setStock(100);
            testProduct.setCreatedAt(LocalDateTime.now());
            testProduct.setUpdatedAt(LocalDateTime.now());
            testProduct = productRepository.save(testProduct);
        } else {
            testProduct = demoProducts.get(0);
        }

        // Delete existing test cart items
        List<CartItem> existingItems = cartRepository.findByVendorId("demo-vendor");
        if (!existingItems.isEmpty()) {
            cartRepository.deleteAll(existingItems);
        }

        // Create test cart items
        List<CartItem> testCartItems = new ArrayList<>();
        
        // Order 1: 2 items for user-001
        CartItem item1 = new CartItem();
        item1.setUserId("test-user-001");
        item1.setProductId(testProduct.getId());
        item1.setVendorId("demo-vendor");
        item1.setQuantity(2);
        item1.setPriceAtAdd(testProduct.getPrice());
        item1.setAddedAt(LocalDateTime.now().minusDays(2));
        item1.setUpdatedAt(LocalDateTime.now().minusDays(2));
        testCartItems.add(item1);

        // Order 1: Another product for same user
        CartItem item2 = new CartItem();
        item2.setUserId("test-user-001");
        item2.setProductId(testProduct.getId());
        item2.setVendorId("demo-vendor");
        item2.setQuantity(1);
        item2.setPriceAtAdd(testProduct.getPrice());
        item2.setAddedAt(LocalDateTime.now().minusDays(2));
        item2.setUpdatedAt(LocalDateTime.now().minusDays(2));
        testCartItems.add(item2);

        // Order 2: Different user
        CartItem item3 = new CartItem();
        item3.setUserId("test-user-002");
        item3.setProductId(testProduct.getId());
        item3.setVendorId("demo-vendor");
        item3.setQuantity(3);
        item3.setPriceAtAdd(testProduct.getPrice());
        item3.setAddedAt(LocalDateTime.now().minusDays(1));
        item3.setUpdatedAt(LocalDateTime.now().minusDays(1));
        testCartItems.add(item3);

        // Save all cart items
        cartRepository.saveAll(testCartItems);

        return ResponseEntity.ok(new DataResponse("Test data added successfully", testCartItems.size()));
    }

    static class CountResponse {
        public long totalCartItems;
        public long demoVendorCartItems;

        public CountResponse(long total, long demoVendor) {
            this.totalCartItems = total;
            this.demoVendorCartItems = demoVendor;
        }
    }

    static class DataResponse {
        public String message;
        public int itemsAdded;

        public DataResponse(String message, int itemsAdded) {
            this.message = message;
            this.itemsAdded = itemsAdded;
        }
    }
}
