package com.saas.platform.catalog.domain.event.key;

import com.saas.platform.common.events.DomainEvent;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyUsedEvent implements DomainEvent {

    private  Long userId;
    private  String username;
    private  String email;
    private String fullName;
    private String role;
    private String androidId;
    private String deviceId;
    private Instant timestamp;



}
