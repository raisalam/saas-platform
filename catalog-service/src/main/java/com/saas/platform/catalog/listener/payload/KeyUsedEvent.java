package com.saas.platform.catalog.listener.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saas.platform.common.events.DomainEvent;
import lombok.*;

import java.time.Instant;

/**
 * Rich event carrying all info handlers may need.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyUsedEvent implements DomainEvent {

    private  String key;
    private  Instant startDate;
    private  Instant endDate;
    private  String details;
    private  String requestId;
    private  String correlationId;
    private String tenantId;
    private  Long userId;


}

