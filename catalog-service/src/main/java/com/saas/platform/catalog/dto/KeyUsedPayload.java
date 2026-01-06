package com.saas.platform.catalog.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class KeyUsedPayload {
    private String key;
    private Boolean notify;
    private Instant usedDate;

}
