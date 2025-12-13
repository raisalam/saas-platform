package com.saas.platform.catalog.domain.event.key;

import com.saas.platform.catalog.dto.KeyGenerationResponse;
import com.saas.platform.catalog.dto.KeyItemResponse;
import com.saas.platform.common.events.DomainEvent;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Rich event carrying all info handlers may need.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyGeneratedEvent implements DomainEvent {
    private String tenantId;
    private  Long userId;
    private  Double balance;
    private  Double totalCost;
    private  String correlationId;
    private KeyGenerationResponse keys;

}
