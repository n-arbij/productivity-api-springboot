package com.jibruski.productivity_api.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDto {
    public record RegisterRequest (
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password
    ){}

    public record LoginRequest (
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record AuthResponse (
        String accessToken,
        String refreshToken
    ){}
}
