package com.shopQ.MainShopQ.products.repository;

import com.shopQ.MainShopQ.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepo extends JpaRepository<ProductImage, Long> {
}

