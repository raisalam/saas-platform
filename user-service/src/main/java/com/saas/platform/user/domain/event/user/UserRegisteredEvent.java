package com.saas.platform.user.domain.event.user;

import com.saas.platform.common.events.DomainEvent;
import com.saas.platform.user.entity.User;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisteredEvent implements DomainEvent {

    private  Long userId;
    private  String username;
    private  String email;
    private String fullName;
    private String role;
    private String androidId;
    private String deviceId;
    private Instant timestamp;



}
