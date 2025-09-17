package com.shopQ.MainShopQ.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StockCheckResult {
    private Long productId;
    private String productName;
    private int requestedQuantity;
    private int availableStock;
    private boolean hasStockShortage;
    private String message;

    public StockCheckResult(Long productId, String productName, int requestedQuantity, int availableStock) {
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
        this.hasStockShortage = requestedQuantity > availableStock;

        if (availableStock == 0) {
            this.message = "Product is out of stock";
        } else if (hasStockShortage) {
            this.message = String.format("Only %d units available, but %d requested", availableStock, requestedQuantity);
        } else {
            this.message = "Stock available";
        }
    }
}
