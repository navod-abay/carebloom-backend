package com.example.carebloom.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carebloom.dto.cart.AddToCartRequest;
import com.example.carebloom.dto.cart.CartItemResponse;
import com.example.carebloom.models.CartItem;
import com.example.carebloom.models.Product;
import com.example.carebloom.repositories.CartRepository;
import com.example.carebloom.repositories.ProductRepository;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Add item to cart or update quantity if exists
     */
    public CartItemResponse addToCart(String userId, AddToCartRequest request) {
        logger.info("Adding item to cart for user: {}, productId: {}", userId, request.getProductId());

        // Validate product exists and is available
        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = productOpt.get();
        if (!product.getIsActive() || !"active".equals(product.getStatus())) {
            throw new IllegalArgumentException("Product is not available");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        // Check if item already in cart
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(userId, request.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update existing item - deduct additional quantity from stock
            cartItem = existingItem.get();
            int additionalQuantity = request.getQuantity();
            int newQuantity = cartItem.getQuantity() + additionalQuantity;
            
            if (product.getStock() < additionalQuantity) {
                throw new IllegalArgumentException("Insufficient stock for additional quantity");
            }
            
            // Deduct additional stock
            product.setStock(product.getStock() - additionalQuantity);
            productRepository.save(product);
            logger.info("Deducted {} units from product {} stock. New stock: {}", additionalQuantity, product.getId(), product.getStock());
            
            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new cart item - deduct stock
            product.setStock(product.getStock() - request.getQuantity());
            productRepository.save(product);
            logger.info("Deducted {} units from product {} stock. New stock: {}", request.getQuantity(), product.getId(), product.getStock());
            
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setVendorId(product.getVendorId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtAdd(product.getPrice());
            cartItem.setAddedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        cartItem = cartRepository.save(cartItem);
        return mapToCartItemResponse(cartItem, product);
    }

    /**
     * Get user's cart items (optimized to reduce N+1 queries)
     */
    public List<CartItemResponse> getCartItems(String userId) {
        logger.info("Fetching cart items for user: {}", userId);

        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract all product IDs
        List<String> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .collect(java.util.stream.Collectors.toList());

        // Fetch all products in one query
        List<Product> products = productRepository.findAllById(productIds);
        
        // Create a map for fast lookup
        java.util.Map<String, Product> productMap = products.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        List<CartItemResponse> responses = new ArrayList<>();
        List<CartItem> itemsToDelete = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                responses.add(mapToCartItemResponse(item, product));
            } else {
                // Product was deleted, mark for removal
                itemsToDelete.add(item);
            }
        }

        // Delete orphaned cart items in batch
        if (!itemsToDelete.isEmpty()) {
            cartRepository.deleteAll(itemsToDelete);
            logger.info("Removed {} orphaned cart items for user: {}", itemsToDelete.size(), userId);
        }

        return responses;
    }

    /**
     * Update cart item quantity - adjusts product stock accordingly
     */
    public CartItemResponse updateCartItem(String userId, String cartItemId, Integer quantity) {
        logger.info("Updating cart item: {} for user: {}", cartItemId, userId);

        Optional<CartItem> cartItemOpt = cartRepository.findById(cartItemId);
        if (cartItemOpt.isEmpty() || !cartItemOpt.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item not found");
        }

        CartItem cartItem = cartItemOpt.get();
        Optional<Product> productOpt = productRepository.findById(cartItem.getProductId());
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = productOpt.get();
        int oldQuantity = cartItem.getQuantity();
        int quantityDifference = quantity - oldQuantity;
        
        // If increasing quantity, check if enough stock available
        if (quantityDifference > 0) {
            if (product.getStock() < quantityDifference) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
            }
            // Deduct additional stock
            product.setStock(product.getStock() - quantityDifference);
            logger.info("Deducted {} units from product {} stock. New stock: {}", quantityDifference, product.getId(), product.getStock());
        } else if (quantityDifference < 0) {
            // Decreasing quantity, restore stock
            product.setStock(product.getStock() + Math.abs(quantityDifference));
            logger.info("Restored {} units to product {} stock. New stock: {}", Math.abs(quantityDifference), product.getId(), product.getStock());
        }
        
        productRepository.save(product);

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItem = cartRepository.save(cartItem);

        return mapToCartItemResponse(cartItem, product);
    }

    /**
     * Remove item from cart - restores stock to product inventory
     */
    public void removeFromCart(String userId, String cartItemId) {
        logger.info("Removing cart item: {} for user: {}", cartItemId, userId);

        Optional<CartItem> cartItemOpt = cartRepository.findById(cartItemId);
        if (cartItemOpt.isPresent() && cartItemOpt.get().getUserId().equals(userId)) {
            CartItem cartItem = cartItemOpt.get();
            
            // Restore stock to product
            Optional<Product> productOpt = productRepository.findById(cartItem.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setStock(product.getStock() + cartItem.getQuantity());
                productRepository.save(product);
                logger.info("Restored {} units to product {} stock. New stock: {}", cartItem.getQuantity(), product.getId(), product.getStock());
            }
            
            cartRepository.delete(cartItem);
        }
    }

    /**
     * Clear user's cart - restores all stock to products
     */
    public void clearCart(String userId) {
        logger.info("Clearing cart for user: {}", userId);
        
        // First restore all stock before deleting cart items
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        for (CartItem cartItem : cartItems) {
            Optional<Product> productOpt = productRepository.findById(cartItem.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setStock(product.getStock() + cartItem.getQuantity());
                productRepository.save(product);
                logger.info("Restored {} units to product {} stock when clearing cart. New stock: {}", 
                    cartItem.getQuantity(), product.getId(), product.getStock());
            }
        }
        
        cartRepository.deleteByUserId(userId);
    }

    /**
     * Get cart item count
     */
    public long getCartItemCount(String userId) {
        return cartRepository.countByUserId(userId);
    }

    /**
     * Map CartItem and Product to CartItemResponse
     */
    private CartItemResponse mapToCartItemResponse(CartItem cartItem, Product product) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setCategory(product.getCategory());
        response.setImageUrl(product.getImageUrl());
        response.setCurrentPrice(product.getPrice());
        response.setPriceAtAdd(cartItem.getPriceAtAdd());
        response.setQuantity(cartItem.getQuantity());
        response.setAvailableStock(product.getStock());
        response.setVendorId(product.getVendorId());
        response.setAddedAt(cartItem.getAddedAt());
        
        // Calculated fields
        response.setItemTotal(cartItem.getPriceAtAdd() * cartItem.getQuantity());
        response.setPriceChanged(!product.getPrice().equals(cartItem.getPriceAtAdd()));
        
        return response;
    }
}