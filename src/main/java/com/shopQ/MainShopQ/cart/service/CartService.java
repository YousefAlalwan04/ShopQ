package com.shopQ.MainShopQ.cart.service;

import com.shopQ.MainShopQ.auth.config.JwtAuthFilter;
import com.shopQ.MainShopQ.auth.repository.UserRepo;
import com.shopQ.MainShopQ.cart.repository.CartRepo;
import com.shopQ.MainShopQ.entity.Cart;
import com.shopQ.MainShopQ.entity.Product;
import com.shopQ.MainShopQ.entity.User;
import com.shopQ.MainShopQ.products.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;

    @Transactional
    public ResponseEntity<String> addToCart(Long productId, int quantity) {
        Product product;

        String currentUserUsername = JwtAuthFilter.CURRENT_USER;
        User currentUser = userRepo.findByUsername(currentUserUsername).get();

        try {
            product = productRepo.findById(productId).get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }


        if (cartRepo.findByProductIdAndUserId(product.getId(), currentUser.getId()).isPresent()) {
            return new ResponseEntity<>("Product is already in cart", HttpStatus.BAD_REQUEST);
        }
        if (product.getQuantity() < quantity) {
            return new ResponseEntity<>("Insufficient stock available, only: " + product.getQuantity() + " in stock", HttpStatus.BAD_REQUEST);
        }


        if (product != null && currentUser != null) {

            Cart cart = new Cart(product, quantity, currentUser);
            cartRepo.save(cart);
            return new ResponseEntity<>("Product added to cart", HttpStatus.OK);

        }
        return null;

    }
    @Transactional
    public ResponseEntity<?> getMyCartItems() {
        try {
            String currentUserUsername = JwtAuthFilter.CURRENT_USER;
            User currentUser = userRepo.findByUsername(currentUserUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));


            if (currentUser == null) {

                return new ResponseEntity<>("you don't have access LMAO LOSER", HttpStatus.UNAUTHORIZED);
            }

            List<Cart> cart = cartRepo.findByUser(currentUser);
            if (cart == null) {
                return new ResponseEntity<>("cart is empty", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(cart, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>("something went wrong will retriving the cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> removeCartItem(Long cartItemId) {
        try {
            String currentUserUsername = JwtAuthFilter.CURRENT_USER;
            User currentUser = userRepo.findByUsername(currentUserUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (currentUser == null) {
                return new ResponseEntity<>("you don't have access LMAO LOSER", HttpStatus.UNAUTHORIZED);
            }

            Cart cartItem = cartRepo.findById(cartItemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            if (!cartItem.getUser().getId().equals(currentUser.getId())) {
                return new ResponseEntity<>("You do not have permission to delete this item", HttpStatus.FORBIDDEN);
            }

            cartRepo.delete(cartItem);
            return new ResponseEntity<>("Cart item removed successfully", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Cart item not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong while removing the cart item", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

