package com.shopQ.MainShopQ.orders.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderConfirmationRequest {
    private String fullName;
    private String fullAdress;
    private List<OrderProductQuantity> orderProductQuantity;
    private boolean acceptPartialOrder; // true if user accepts available quantity

}
