package com.shopQ.MainShopQ.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_address", nullable = false)
    private String cutomerAddress;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false, columnDefinition = "INT CHECK (quantity > 0)")
    @ColumnDefault("1")
    private int quantity;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "order_status", nullable = false)
    @ColumnDefault("'PENDING'")
    private String orderStatus;

    @Column(name = "order_date", nullable = false)
    private Date orderDate;

    @ManyToOne
    @JoinColumn(name = "product_product_id", nullable = false, unique = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, unique = false)
    private User user;

    public Order(String customerName,
                 String cutomerAddress,
                 String customerEmail,
                 String productName,
                 int quantity,
                 double price,
                 String orderStatus,
                 Date orderDate,
                 Product product,
                 User user) {
        this.customerName = customerName;
        this.cutomerAddress = cutomerAddress;
        this.customerEmail = customerEmail;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
        this.product = product;
        this.user = user;
    }
}
