package com.saas.platform.user.listener.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saas.platform.common.events.DomainEvent;
import lombok.*;

/**
 * Rich event carrying all info handlers may need.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyGeneratedEvent implements DomainEvent {
    private String tenantId;
    private  Long userId;
    private  Double balance;
    private  Double totalCost;
    private  String correlationId;

}
