package org.example.novelreader.service;

import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.LoginRequest;
import org.example.novelreader.dto.LoginResponse;
import org.example.novelreader.entity.User;
import org.example.novelreader.security.CustomUserDetailsService;
import org.example.novelreader.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userDetailsService.findUserByUsernameOrEmail(request.getUsernameOrEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Nieprawidłowe hasło");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        return LoginResponse.of(token, user.getId(), user.getUsername(), user.getEmail());
    }
}

