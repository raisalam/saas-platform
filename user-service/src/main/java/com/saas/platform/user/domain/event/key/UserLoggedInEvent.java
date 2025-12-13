package com.saas.platform.user.domain.event.key;

import com.saas.platform.common.events.DomainEvent;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * Rich event carrying all info handlers may need.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoggedInEvent implements DomainEvent {
    private String tenantId;
    private  Long userId;
    private  String email;
    private  String name;
    private  String androidId;
    private  String deviceId;
    private  String ip;
    private  Instant timestamp;
    private  Double balance;
    private  Double total;
    private  String role;
    private String correlationId;
    private  boolean active;
    private  Map<String, Object> meta;

}
