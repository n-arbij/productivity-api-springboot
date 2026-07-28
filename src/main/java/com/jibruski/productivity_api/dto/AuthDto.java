package com.jibruski.productivity_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {
    public record RegisterRequest (
        @NotBlank @Size(min = 3) String username,
        @NotBlank @Email String email,
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
