package org.example.novelreader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type;
    private Long userId;
    private String username;
    private String email;

    public static LoginResponse of(String token, Long userId, String username, String email) {
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userId)
                .username(username)
                .email(email)
                .build();
    }
}

