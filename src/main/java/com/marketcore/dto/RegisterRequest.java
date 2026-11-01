package com.marketcore.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String role; // Ex: "ROLE_ADMIN" ou "ROLE_USER"
}