package com.shopQ.MainShopQ.products.service;

import com.shopQ.MainShopQ.entity.Product;
import com.shopQ.MainShopQ.products.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.security.PrivateKey;

@Service
public class ProductService {

    @Autowired
    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
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
    public Iterable<Product> getAllProducts(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 1);
        return productRepo.findAll(pageable);
    }

    public Product getProductById(Long id) {


        return productRepo.findById(id).orElse(null);
    }

    public Product getProductByName(String name) {
        return productRepo.findByProductName(name).orElse(null);
    }

}
