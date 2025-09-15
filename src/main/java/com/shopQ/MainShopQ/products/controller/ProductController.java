package com.shopQ.MainShopQ.products.controller;

import com.shopQ.MainShopQ.entity.Product;
import com.shopQ.MainShopQ.entity.ProductImage;
import com.shopQ.MainShopQ.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    public Iterable<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping(value = {"/add"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Product addNewProduct(@RequestPart("product") Product product,
                                 @RequestPart("image") MultipartFile[] files) {
        try{
            Set<ProductImage> images = uploadImage(files);
            product.setImages(images);
            return productService.addNewProduct(product);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<ProductImage> uploadImage(MultipartFile[] multipartFiles) throws IOException {
        Set<ProductImage> productImages = new HashSet<>();
        for (MultipartFile file : multipartFiles) {
            ProductImage image = new ProductImage(
                    file.getOriginalFilename()
                    ,file.getContentType()
                    ,file.getBytes());
            productImages.add(image);
        }
        return productImages;
    }

    @PutMapping("/update")
    public Product updateProduct(@RequestBody Product product) {
        return productService.updateProduct(product);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
    }


    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }


    @GetMapping("/name/{name}")
    public Product getProductByName(@PathVariable String name) {
        return productService.getProductByName(name);
    }
}
