package com.shopQ.MainShopQ.cart.controller;

import com.shopQ.MainShopQ.cart.service.CartService;
import com.shopQ.MainShopQ.entity.Cart;
import com.shopQ.MainShopQ.entity.Product;
import com.shopQ.MainShopQ.products.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private ProductRepo productRepo;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/add/{productId}/{quantity}")
    public ResponseEntity<String> addToCart(@RequestParam(name = "productId") Long productId,
                                            @RequestParam(name = "quantity", defaultValue = "1") int quantity) {
        return cartService.addToCart(productId, quantity);


    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("my-cart")
    public ResponseEntity<?> getMyCartItems() {
        return cartService.getMyCartItems();
    }

}
