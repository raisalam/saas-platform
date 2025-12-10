package com.saas.platform.user.domain.event.user;

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
    private  Long userId;
    private  String email;
    private  String name;
    private  String androidId;
    private  String deviceId;
    private  String ip;
    private  Instant timestamp;
    private  Double balance;
    private  Map<String, Object> meta;

}
