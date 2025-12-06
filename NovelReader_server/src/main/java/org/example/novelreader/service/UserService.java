package org.example.novelreader.service;

import lombok.RequiredArgsConstructor;
import org.example.novelreader.dto.RegisterRequest;
import org.example.novelreader.dto.RegisterResponse;
import org.example.novelreader.entity.User;
import org.example.novelreader.exception.UserAlreadyExistsException;
import org.example.novelreader.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Użytkownik o podanej nazwie już istnieje");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Użytkownik o podanym adresie email już istnieje");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("Rejestracja zakończona pomyślnie")
                .build();
    }
}

