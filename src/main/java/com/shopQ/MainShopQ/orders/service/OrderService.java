package com.shopQ.MainShopQ.orders.service;

import com.shopQ.MainShopQ.auth.config.JwtAuthFilter;
import com.shopQ.MainShopQ.auth.repository.UserRepo;
import com.shopQ.MainShopQ.entity.*;
import com.shopQ.MainShopQ.orders.dto.*;
import com.shopQ.MainShopQ.orders.repository.OrderRepository;
import com.shopQ.MainShopQ.products.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderService {



    private static final String ORDER_STATUS_PLACED = "Placed";

    @Autowired
    private final ProductRepo productRepo;

    @Autowired
    private final UserRepo userRepo;

    @Autowired
    private final OrderRepository orderRepository;

    public OrderService(ProductRepo productRepo, UserRepo userRepo, OrderRepository orderRepository) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.orderRepository = orderRepository;
    }


    public OrderValidationResponse validateOrder(OrderItem orderItem) {
        List<OrderProductQuantity> productQuantityList = orderItem.getOrderProductQuantity();

        if (productQuantityList == null || productQuantityList.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one product");
        }

        String currentUserUsername = JwtAuthFilter.CURRENT_USER;
        if (currentUserUsername == null || currentUserUsername.isEmpty()) {
            throw new IllegalStateException("User not authenticated");
        }

        List<StockCheckResult> stockIssues = new ArrayList<>();
        boolean hasStockIssues = false;

        for (OrderProductQuantity pq : productQuantityList) {
            if (pq.getProductId() == null) {
                throw new IllegalArgumentException("Product ID cannot be null");
            }

            if (pq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            Product product = productRepo.findById(pq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + pq.getProductId()));

            StockCheckResult stockCheck = new StockCheckResult(
                product.getId(),
                product.getProductName(),
                pq.getQuantity(),
                product.getQuantity()
            );

            // If there's any stock issue add to list
            if (product.getQuantity() <= 0 || pq.getQuantity() > product.getQuantity()) {
                stockIssues.add(stockCheck);
                hasStockIssues = true;
            }
        }

        OrderValidationResponse response = new OrderValidationResponse();
        response.setCanProceed(!hasStockIssues);
        response.setStockIssues(stockIssues);

        if (hasStockIssues) {
            response.setMessage("Some items in your order have stock issues. Please review and confirm your order.");
        } else {
            response.setMessage("All items are available. You can proceed with the order.");
        }

        return response;
    }

   // Confirm order after user has reviewed stock issues
    @Transactional
    public String confirmOrder(OrderConfirmationRequest confirmationRequest) {

        String currentUserUsername = JwtAuthFilter.CURRENT_USER;
        if (currentUserUsername == null || currentUserUsername.isEmpty()) {
            throw new IllegalStateException("User not authenticated");
        }

        User user = userRepo.findByUsername(currentUserUsername)
                .orElseThrow(() -> new IllegalStateException("User not found: " + currentUserUsername));

        StringBuilder orderSummary = new StringBuilder("Order confirmed and processed:\n");
        Date orderDate = new Date();

        for (OrderProductQuantity item : confirmationRequest.getOrderProductQuantity()) {
            if (item.getQuantity() <= 0) {
                continue; // Skip items with 0 quantity (user changed their mind)
            }

            Product product = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + item.getProductId()));

            // Final stock validation before processing
            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getProductName() +
                    ". Available: " + product.getQuantity() + ", Requested: " + item.getQuantity());
            }

            if (product.getQuantity() <= 0) {
                throw new IllegalStateException("Product '" + product.getProductName() + "' is out of stock");
            }

            try {
                Order order = new Order(
                        confirmationRequest.getFullName(),
                        confirmationRequest.getFullAdress(),
                        user.getEmail(),
                        product.getProductName(),
                        item.getQuantity(),
                        product.getPrice() * item.getQuantity(),
                        ORDER_STATUS_PLACED,
                        orderDate,
                        product,
                        user
                );

                // Update the available product quantity
                product.setQuantity(product.getQuantity() - item.getQuantity());


                productRepo.save(product);
                orderRepository.save(order);

                orderSummary.append("- ").append(product.getProductName())
                           .append(": ").append(item.getQuantity())
                           .append(" units ordered for $").append(product.getPrice() * item.getQuantity())
                           .append("\n");

            } catch (Exception e) {
                throw new RuntimeException("Failed to process order for product: " + product.getProductName() + ". Error: " + e.getMessage(), e);
            }
        }

        return orderSummary.toString();
    }

    // Place order directly if theres no stock issues (auto-confirm)
    @Transactional
    public String placeOrder(OrderItem orderItem) {
        OrderValidationResponse validation = validateOrder(orderItem);

        if (!validation.isCanProceed()) {

            StringBuilder errorMessage = new StringBuilder("Stock issues found:\n");
            for (StockCheckResult issue : validation.getStockIssues()) {
                errorMessage.append("- ").append(issue.getMessage()).append("\n");
            }
            errorMessage.append("\nPlease use the /validate-order and /confirm-order endpoints for better control.");
            throw new IllegalStateException(errorMessage.toString());
        }

        // If no issues, auto confirm with original quantities
        OrderConfirmationRequest autoConfirm = new OrderConfirmationRequest();
        autoConfirm.setFullName(orderItem.getFullName());
        autoConfirm.setFullAdress(orderItem.getFullAdress());
        autoConfirm.setAcceptPartialOrder(false);

        List<OrderProductQuantity> confirmedItems = new ArrayList<>();
        for (OrderProductQuantity pq : orderItem.getOrderProductQuantity()) {
            OrderProductQuantity confirmedItem = new OrderProductQuantity();
            confirmedItem.setProductId(pq.getProductId());
            confirmedItem.setQuantity(pq.getQuantity());
            confirmedItems.add(confirmedItem);
        }
        autoConfirm.setOrderProductQuantity(confirmedItems);

        return confirmOrder(autoConfirm);
    }

    public List<Order> getCurrentUserOrders() {
        String currentUserUsername = JwtAuthFilter.CURRENT_USER;
        if (currentUserUsername == null || currentUserUsername.isEmpty()) {

            throw new IllegalStateException("User not authenticated");
        }
        User user = userRepo.findByUsername(currentUserUsername)
                .orElseThrow(() -> new IllegalStateException("User not found: " + currentUserUsername));

        return orderRepository.findAllByUserId(user.getId());
    }

    public ResponseEntity<?> getAllOrders(){
        if (orderRepository.findAll().isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No orders found");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(orderRepository.findAll(), HttpStatus.OK);
    }
}
