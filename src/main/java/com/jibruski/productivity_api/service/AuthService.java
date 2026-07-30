package com.jibruski.productivity_api.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jibruski.exceptionstarter.exceptions.ConflictException;
import com.jibruski.exceptionstarter.exceptions.UnauthorizedException;
import com.jibruski.productivity_api.dto.AuthDto.AuthResponse;
import com.jibruski.productivity_api.dto.AuthDto.LoginRequest;
import com.jibruski.productivity_api.dto.AuthDto.RegisterRequest;
import com.jibruski.productivity_api.model.User;
import com.jibruski.productivity_api.repository.UserRepository;
import com.yourorg.jwtauth.model.UserPrincipal;
import com.yourorg.jwtauth.service.JwtService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register (RegisterRequest request){
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        if (userRepository.findByUsername(request.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    public AuthResponse login (LoginRequest request){
        User user = userRepository.findByUsername(request.username()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid Credentials");
        }

        UserPrincipal principal = new UserPrincipal(user.getId().toString(), List.of("USER"));
        String accessToken = jwtService.issueAccessToken(principal);
        String refreshToken = jwtService.issueRefreshToken(principal);        
        
        return new AuthResponse(accessToken, refreshToken);
    }

    public String refreshAccessToken(String refreshToken) {
        Claims claims = jwtService.parseAndValidate(refreshToken);
        if (!jwtService.isRefreshToken(claims)) {
            throw new UnauthorizedException("Invalid credentials");
        }
        UserPrincipal principal = jwtService.toPrincipal(claims);
        return jwtService.issueAccessToken(principal);
    }
}
