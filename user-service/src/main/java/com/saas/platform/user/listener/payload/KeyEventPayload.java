package com.saas.platform.user.listener.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class KeyEventPayload {
    private Long userId;
    private List<String> keys;
    private Double balance;
    private Double totalCost;
}
