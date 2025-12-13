package com.saas.platform.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private long id;
    private String token;
    private String refreshToken;
    private String fullName;
    private String role;
    private String avatarUrl;
}
