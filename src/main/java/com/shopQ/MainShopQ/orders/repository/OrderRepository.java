package com.shopQ.MainShopQ.orders.repository;

import com.shopQ.MainShopQ.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    @Query("FROM Order o WHERE o.user.id = :userId")
    List<Order> findAllByUserId(@Param("userId")Long userId);

    boolean existsByProduct_Id(Long productId);
}
