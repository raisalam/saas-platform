package com.saas.platform.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodayStatsResponse {
    private long keysGenerated;
    private long keysUsed;
    private Double totalSpent;
}
