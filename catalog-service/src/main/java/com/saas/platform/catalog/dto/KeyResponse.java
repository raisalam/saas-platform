package com.saas.platform.catalog.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeyResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Double balance;
    private Double total;
    private boolean active;
}
