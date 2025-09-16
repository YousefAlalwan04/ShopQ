package com.shopQ.MainShopQ.products.repository;

import com.shopQ.MainShopQ.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {
    Optional<Product> findByProductName(String productName);

}
