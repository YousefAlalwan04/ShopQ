package com.shopQ.MainShopQ.products.repository;

import com.shopQ.MainShopQ.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {
    Optional<Product> findByProductName(String productName);
    Page<Product> findAll(Pageable pageable);
    
}
