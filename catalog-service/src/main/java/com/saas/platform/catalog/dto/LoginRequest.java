package com.saas.platform.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginRequest {

    @NotBlank(message = "Username or Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Android ID is required")
    private String androidId;

    private DeviceInfoDto deviceInfo;
    private MetadataDto metadata;
}
