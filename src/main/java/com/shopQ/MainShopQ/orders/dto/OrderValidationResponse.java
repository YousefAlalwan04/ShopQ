package com.shopQ.MainShopQ.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderValidationResponse {
    private boolean canProceed;
    private List<StockCheckResult> stockIssues;
    private String message;

    public OrderValidationResponse() {}
}
