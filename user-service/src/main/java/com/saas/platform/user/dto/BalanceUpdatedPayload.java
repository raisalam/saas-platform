package com.saas.platform.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BalanceUpdatedPayload {
    private Double balance;
    private Boolean notify;
}
