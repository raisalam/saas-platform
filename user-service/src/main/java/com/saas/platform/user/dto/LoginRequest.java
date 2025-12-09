package com.saas.platform.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username or Email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Android ID is required")
    private String androidId;

    @NotBlank(message = "Android ID is required")
    private String deviceId;
}
