package com.shopQ.MainShopQ.auth.repository;

import com.shopQ.MainShopQ.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findById(Long aLong);

    Optional<User> findByEmail(String username);

    Optional<User> findByUsername(String username);
}
