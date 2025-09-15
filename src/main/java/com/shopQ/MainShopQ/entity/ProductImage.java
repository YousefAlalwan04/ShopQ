package com.shopQ.MainShopQ.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "image_bytes", nullable = false, length = 5000000)
    private byte[] imageBytes;

    public ProductImage(String name, String type, byte[] imageBytes) {
        this.name = name;
        this.type = type;
        this.imageBytes = imageBytes;
    }
}
