package com.shopQ.MainShopQ.orders.controller;

import com.shopQ.MainShopQ.entity.Order;
import com.shopQ.MainShopQ.orders.dto.OrderItem;
import com.shopQ.MainShopQ.orders.dto.OrderConfirmationRequest;
import com.shopQ.MainShopQ.orders.dto.OrderValidationResponse;
import com.shopQ.MainShopQ.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // check if the quantity of the wanted product is available and return issues to user
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/validate-order/{isCartCheckout}")
    public ResponseEntity<Map<String, Object>> validateOrder(@PathVariable(name  = "isCartCheckout") boolean isCartCheckout,
                                                             @RequestBody OrderItem orderItem) {
        Map<String, Object> response = new HashMap<>();

        try {
            OrderValidationResponse validation = orderService.validateOrder(isCartCheckout ,orderItem);
            response.put("success", true);
            response.put("canProceed", validation.isCanProceed());
            response.put("message", validation.getMessage());
            response.put("stockIssues", validation.getStockIssues());

            if (validation.isCanProceed()) {
                response.put("instruction", "All items are available. You can proceed with /place-order or /confirm-order");
            } else {
                response.put("instruction", "Please review the stock issues and use /confirm-order with your adjusted quantities");
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Invalid request");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("error", "Authentication issue");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Internal server error");
            response.put("message", "Failed to validate order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

   //Confirm order after the user has reviewed the stock issues
    // this api cant be used to place orders from the cart directly
    //to checkout cart use /place-order/{isCartCheckout} with isCartCheckout = true
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/confirm-order")
    public ResponseEntity<Map<String, Object>> confirmOrder(@RequestBody OrderConfirmationRequest confirmationRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String orderSummary = orderService.confirmOrder(false,confirmationRequest);
            response.put("success", true);
            response.put("message", "Order confirmed and placed successfully");
            response.put("orderDetails", orderSummary);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Invalid request");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("error", "Stock/Authentication issue");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Internal server error");
            response.put("message", "Failed to process order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    //Directly place order if in stock
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place-order/{isCartCheckout}")
    public ResponseEntity<Map<String, Object>> placeOrder(@PathVariable(name = "isCartCheckout") boolean isCartCheckout,
                                                          @RequestBody(required = false) OrderItem orderItem) {
        Map<String, Object> response = new HashMap<>();

        // Validate input for a non-cart checkout
        if (!isCartCheckout && orderItem == null) {
            response.put("success", false);
            response.put("error", "Order item is required for non-cart checkout");
            return ResponseEntity.badRequest().body(response);
        }

        // For cart checkout, if no orderItem provided, user details are required
        if (isCartCheckout && (orderItem == null ||
            (orderItem.getFullName() == null || orderItem.getFullName().isEmpty()) ||
            (orderItem.getFullAdress() == null || orderItem.getFullAdress().isEmpty()))) {
            response.put("success", false);
            response.put("error", "Full name and address are required for cart checkout");
            response.put("message", "Please provide customer details: {\"fullName\": \"Your Name\", \"fullAdress\": \"Your Address\"}");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String orderSummary = orderService.placeOrder(isCartCheckout, orderItem);
            response.put("success", true);
            response.put("message", "Order placed successfully");
            response.put("details", orderSummary);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Invalid request");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("error", "Stock/Authentication issue");
            response.put("message", e.getMessage());
            response.put("suggestion", "Use /validate-order first to check stock availability and then /confirm-order to proceed with adjusted quantities");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Internal server error");
            response.put("message", "Failed to process order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/order-history")
    public List<Order> getOrderHistory(){
        return orderService.getCurrentUserOrders();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-orders")
    public ResponseEntity<?> getAllOrders() {
        return orderService.getAllOrders();
    }
}
