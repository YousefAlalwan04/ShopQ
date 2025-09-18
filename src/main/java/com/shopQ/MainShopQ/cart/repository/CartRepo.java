package com.shopQ.MainShopQ.cart.repository;

import com.shopQ.MainShopQ.entity.Cart;
import com.shopQ.MainShopQ.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    Optional<Cart> findByProductIdAndUserId(Long productId, Long userId);

    List<Cart> findByUser(User user);
}
