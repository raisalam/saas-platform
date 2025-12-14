package com.saas.platform.user.listener.payload;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyItemResponse {

    private Long id;
    private String keyValue;
    private boolean used;
    private Instant usedDate;
    private Long planId;
    private Long sellerId;
    private Long gameId;
    private Instant createdDate;
    private Instant updatedDate;
    private Long durationMinutes;
}
