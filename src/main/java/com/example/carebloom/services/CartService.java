package com.example.carebloom.services;

import com.example.carebloom.dto.cart.AddToCartRequest;
import com.example.carebloom.dto.cart.CartItemResponse;
import com.example.carebloom.models.CartItem;
import com.example.carebloom.models.Product;
import com.example.carebloom.repositories.CartRepository;
import com.example.carebloom.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            // Update existing item
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock for total quantity");
            }
            
            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new cart item
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
     * Update cart item quantity
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
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItem = cartRepository.save(cartItem);

        return mapToCartItemResponse(cartItem, product);
    }

    /**
     * Remove item from cart
     */
    public void removeFromCart(String userId, String cartItemId) {
        logger.info("Removing cart item: {} for user: {}", cartItemId, userId);

        Optional<CartItem> cartItemOpt = cartRepository.findById(cartItemId);
        if (cartItemOpt.isPresent() && cartItemOpt.get().getUserId().equals(userId)) {
            cartRepository.delete(cartItemOpt.get());
        }
    }

    /**
     * Clear user's cart
     */
    public void clearCart(String userId) {
        logger.info("Clearing cart for user: {}", userId);
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