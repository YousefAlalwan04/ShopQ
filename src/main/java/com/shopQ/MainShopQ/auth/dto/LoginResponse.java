package com.shopQ.MainShopQ.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String role;
    private Long expiresIn;

    public LoginResponse(String token, String role, Long expiresIn) {
        this.token = token;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}
