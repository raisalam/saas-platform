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
public class BalanceUpdatedEvent implements DomainEvent {
    private  Long userId;
    private  Double balance;
}
