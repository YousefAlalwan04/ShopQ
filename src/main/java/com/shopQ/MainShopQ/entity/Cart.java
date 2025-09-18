package com.shopQ.MainShopQ.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cartId;

    @ManyToOne
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @ManyToOne
    private User user;

    public Cart(Product product, int quantity, User user) {
        this.product = product;
        this.quantity = quantity;
        this.user = user;

    }
}
