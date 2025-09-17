package com.shopQ.MainShopQ.orders.repository;

import com.shopQ.MainShopQ.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    @Query("FROM Order o WHERE o.user.id = :userId")
    List<Order> findAllByUserId(@Param("userId")Long userId);

}
