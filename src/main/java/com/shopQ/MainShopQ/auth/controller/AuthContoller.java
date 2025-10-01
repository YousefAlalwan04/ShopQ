package com.shopQ.MainShopQ.auth.controller;

import com.shopQ.MainShopQ.auth.dto.LoginRequest;
import com.shopQ.MainShopQ.auth.dto.LoginResponse;
import com.shopQ.MainShopQ.auth.dto.RegisterRequest;
import com.shopQ.MainShopQ.auth.service.AuthService;
import com.shopQ.MainShopQ.auth.service.JwtService;
import com.shopQ.MainShopQ.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthContoller {
    private final JwtService jwtService;
    private final AuthService authService;

    public AuthContoller(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest) {
        User registeredUser = authService.signup(registerRequest);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        User authenticatedUser = authService.authenticate(loginRequest);
        String token = jwtService.generateToken(authenticatedUser);
        LoginResponse response = new LoginResponse(token, authenticatedUser.getRole(), jwtService.getExpiration());
        return ResponseEntity.ok(response);
    }

}
