package com.saas.platform.user.dto;

import com.saas.platform.user.entity.ActivityType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserActivityDto {

    private Long id;
    private ActivityType activityType;

    private String title;
    private String message;

    private Double amount;
    private Double balanceAfter;

    /**
     * Raw JSON string.
     * You can later change this to Map<String, Object> if needed.
     */
    private String metadata;


    private LocalDateTime createdAt;
}
