package com.saas.platform.user.domain.event.key;

import com.saas.platform.common.events.DomainEvent;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRechargedEvent implements DomainEvent {

    private String tenantId;
    private  Long parentUserId;
    private Double parentRechargeAmount;
    private Double parentPreviousAmount;
    private Double parentCurrentAmount;
    private Double parentTotalAmount;
    private String parentRole;
    private boolean parentActive;

    private  Long childUserId;
    private Double childRechargeAmount;
    private Double childPreviousAmount;
    private Double childCurrentAmount;
    private Double childTotalAmount;
    private String childRole;
    private boolean childActive;
    private String correlationId;





    private Instant timestamp;
}
