package com.saas.platform.catalog.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyGenerationResponse {
    private List<KeyItemResponse> keys;
}
