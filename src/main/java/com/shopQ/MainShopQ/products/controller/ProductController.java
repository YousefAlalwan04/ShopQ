package com.shopQ.MainShopQ.products.controller;

import com.shopQ.MainShopQ.entity.Product;
import com.shopQ.MainShopQ.entity.ProductImage;
import com.shopQ.MainShopQ.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    public Iterable<Product> getAllProducts(@RequestParam(defaultValue = "0") int pageNumber,
                                            @RequestParam(defaultValue = "") String filter) {
        return productService.getAllProducts(pageNumber, filter);
    }
    @PreAuthorize("hasRole('ADMIN')")
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
        public ResponseEntity<?> deleteProductById(@PathVariable Long id) {
            try {
                productService.deleteProductById(id);
                return ResponseEntity.noContent().build();
            } catch (IllegalStateException e) {
                Map<String, String> body = new HashMap<>();
                body.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
            } catch (Exception e) {
                Map<String, String> body = new HashMap<>();
                body.put("message", "Failed to delete product");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
            }
        }
    
    
        @GetMapping("/{id}")
        public Product getProductById(@PathVariable Long id) {
            return productService.getProductById(id);
        }

    @GetMapping("/get-products-to-checkout/")
    public ResponseEntity<?> getProductsDetialsForCheckout(@RequestParam(name = "isSingleProduct") boolean isSingleProduct,
                                                           @RequestParam(name = "productId") Long productId) {
         return productService.getProductsDetialsForCheckout(isSingleProduct, productId);
        }



    @GetMapping("/name/{name}")
    public Product getProductByName(@PathVariable String name) {
        return productService.getProductByName(name);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/update-with-images", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Product updateProductWithImages(@RequestPart("product") Product product,
                                           @RequestPart(name = "addImages", required = false) MultipartFile[] addImages,
                                           @RequestParam(name = "removeImageIds", required = false) List<Long> removeImageIds) {
        try {
            return productService.updateProductWithImages(product, addImages, removeImageIds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
