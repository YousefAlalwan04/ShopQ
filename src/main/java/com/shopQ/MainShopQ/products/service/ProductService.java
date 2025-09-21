package com.shopQ.MainShopQ.products.service;

import com.shopQ.MainShopQ.auth.config.JwtAuthFilter;
import com.shopQ.MainShopQ.auth.repository.UserRepo;
import com.shopQ.MainShopQ.cart.repository.CartRepo;
import com.shopQ.MainShopQ.entity.Cart;
import com.shopQ.MainShopQ.entity.Product;
import com.shopQ.MainShopQ.entity.User;
import com.shopQ.MainShopQ.products.repository.ProductRepo;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.security.PrivateKey;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private final ProductRepo productRepo;

    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private CartRepo cartRepo;

    public ProductService(ProductRepo productRepo, UserRepo userRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }

    public Product addNewProduct(Product product) {
        return productRepo.save(product);
    }

    public void deleteProductById(Long id) {
        productRepo.deleteById(id);
    }

    public Product updateProduct(Product product) {
        return productRepo.save(product);
    }

    public Iterable<Product> getAllProducts(int pageNumber, String filterKey) {
        Pageable pageable = PageRequest.of(pageNumber, 5);

        if (!filterKey.isEmpty()) {
            return productRepo.findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    filterKey, filterKey, pageable
            );
        } else {
            return productRepo.findAll(pageable);
        }
    }

    public Product getProductById(Long id) {


        return productRepo.findById(id).orElse(null);
    }

    public Product getProductByName(String name) {
        return productRepo.findByProductName(name).orElse(null);
    }

    public ResponseEntity<?> getProductsDetialsForCheckout(boolean isSingleProduct, Long productId) {
        if (isSingleProduct && productId != null) {

            Product product = getProductById(productId);
            if (product == null) {
                return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok().body(product);

        } else {
            String currentUserUsername = JwtAuthFilter.CURRENT_USER;
            if (currentUserUsername == null || currentUserUsername.isEmpty()) {
                return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
            }

            User user = userRepo.findByUsername(currentUserUsername).orElse(null);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            List<Cart> carts = cartRepo.findByUser(user);
            if (carts == null || carts.isEmpty()) {
                return new ResponseEntity<>("Cart is empty", HttpStatus.NOT_FOUND);
            }

            List<Product> products = carts.stream()
                    .map(Cart::getProduct)
                    .collect(Collectors.toList());

            return ResponseEntity.ok().body(products);
        }
    }
}