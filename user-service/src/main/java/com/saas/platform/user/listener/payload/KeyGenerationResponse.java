package com.saas.platform.user.listener.payload;

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
