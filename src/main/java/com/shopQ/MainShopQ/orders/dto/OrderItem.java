package com.shopQ.MainShopQ.orders.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderItem {

    private String fullName;
    private String fullAdress;

    private Date orderDate;
    private List<OrderProductQuantity> orderProductQuantity;

}
