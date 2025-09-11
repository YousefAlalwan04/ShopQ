package com.shopQ.MainShopQ.auth.service;

import com.shopQ.MainShopQ.auth.dto.LoginRequest;
import com.shopQ.MainShopQ.auth.dto.RegisterRequest;
import com.shopQ.MainShopQ.auth.repository.UserRepo;
import com.shopQ.MainShopQ.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signup(RegisterRequest request) {
        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()),  request.getEmail(), request.getRole());

        return userRepo.save(user);
    }

    public User authenticate(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");

        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        return user;
    }

}
