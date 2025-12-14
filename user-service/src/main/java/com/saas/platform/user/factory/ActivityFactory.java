package com.saas.platform.user.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.platform.user.entity.ActivityType;
import com.saas.platform.user.entity.UserActivity;
import com.saas.platform.user.listener.payload.KeyItemResponse;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityFactory {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static UserActivity keyGenerated(
            Long userId,
            List<KeyItemResponse> keys,
            Double amount,
            Double balance,
            String correlationId
    ) throws JsonProcessingException {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalKeys", keys.size());

        List<Map<String, Object>> keyDetails = keys.stream()
                .map(k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("key", k.getKeyValue());
                    m.put("gameId", k.getGameId());
                    m.put("planId", k.getPlanId());
                    m.put("durationMinutes", k.getDurationMinutes());
                    return m;
                })
                .toList();

        metadata.put("keys", keyDetails);
        UserActivity userActivity =  UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.KEY_GENERATED)
                .title("Keys Generated")
                .message(keys.size() + " keys generated")
                .amount(amount)
                .balanceAfter(balance)
                .metadata(objectMapper.writeValueAsString(metadata))
                .correlationId(correlationId)
                .createdAt(LocalDateTime.now())
                .build();
        System.out.println("=================building user logs=========");
        System.out.println(userActivity);
        System.out.println("============================================");

        return userActivity;
    }

    public static UserActivity keyUsed(
            Long userId,
            String game,
            Double balance,
            String correlationId
    ) {
        UserActivity userActivity = UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.KEY_USED)
                .title("Key Used")
                .message("Key used for " + game)
                .amount(0D)
                .balanceAfter(balance)
                .createdAt(LocalDateTime.now())
                .correlationId(correlationId)
                .build();

        System.out.println("=================building user logs=========");
        System.out.println(userActivity);
        System.out.println("============================================");

        return userActivity;
    }

    public static UserActivity balanceReceived(
            Long userId,
            Double amount,
            Double balance,
            Map<String, String> metadata
    ) throws JsonProcessingException {
        UserActivity userActivity = UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.BALANCE_RECEIVED)
                .title("Balance Received")
                .message("$" + amount + " credited")
                .amount(amount)
                .balanceAfter(balance)
                .createdAt(LocalDateTime.now())
                .correlationId(MDC.get("traceId"))
                .metadata(objectMapper.writeValueAsString(metadata))
                .build();
        System.out.println("=================building user logs=========");
        System.out.println(userActivity);
        System.out.println("============================================");

        return userActivity;
    }

    public static UserActivity balanceSent(
            Long userId,
            Double amount,
            Double balance,
            Map<String, String> metadata
    ) throws JsonProcessingException {
        UserActivity userActivity = UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.BALANCE_SENT)
                .title("Balance Sent")
                .message("$" + amount + " sent")
                .amount(0 - amount)
                .balanceAfter(balance)
                .createdAt(LocalDateTime.now())
                .correlationId(MDC.get("traceId"))
                .metadata(objectMapper.writeValueAsString(metadata))
                .build();
        System.out.println("=================building user logs=========");
        System.out.println(userActivity);
        System.out.println("============================================");

        return userActivity;
    }
}
