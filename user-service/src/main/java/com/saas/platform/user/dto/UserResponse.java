package com.saas.platform.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Double balance;
    private boolean active;
    private String avatarUrl;

}
