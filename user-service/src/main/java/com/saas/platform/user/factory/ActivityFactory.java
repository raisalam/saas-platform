package com.saas.platform.user.factory;

import com.saas.platform.user.entity.ActivityType;
import com.saas.platform.user.entity.UserActivity;
import org.slf4j.MDC;

import java.time.LocalDateTime;

public class ActivityFactory {

    public static UserActivity keyGenerated(
            Long userId,
            int keys,
            Double amount,
            Double balance
    ) {
        UserActivity userActivity =  UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.KEY_GENERATED)
                .title("Keys Generated")
                .message(keys + " keys generated")
                .amount(amount)
                .balanceAfter(balance)
                .metadata("""
                    { "keys": %d }
                """.formatted(keys))
                .correlationId(MDC.get("traceId"))
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
            Double balance
    ) {
        UserActivity userActivity = UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.KEY_USED)
                .title("Key Used")
                .message("Key used for " + game)
                .amount(0D)
                .balanceAfter(balance)
                .createdAt(LocalDateTime.now())
                .build();

        System.out.println("=================building user logs=========");
        System.out.println(userActivity);
        System.out.println("============================================");

        return userActivity;
    }

    public static UserActivity balanceReceived(
            Long userId,
            Double amount,
            Double balance
    ) {
        UserActivity userActivity = UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.BALANCE_RECEIVED)
                .title("Balance Received")
                .message("$" + amount + " credited")
                .amount(amount)
                .balanceAfter(balance)
                .createdAt(LocalDateTime.now())
                .build();
        System.out.println("=================building user logs=========");
        System.out.println(userActivity);
        System.out.println("============================================");

        return userActivity;
    }

    public static UserActivity balanceSent(
            Long userId,
            Double amount,
            Double balance
    ) {
        UserActivity userActivity = UserActivity.builder()
                .userId(userId)
                .activityType(ActivityType.BALANCE_SENT)
                .title("Balance Sent")
                .message("$" + amount + " sent")
                .amount(0 - amount)
                .balanceAfter(balance)
                .createdAt(LocalDateTime.now())
                .correlationId(MDC.get("traceId"))
                .build();
        System.out.println("=================building user logs=========");
        System.out.println(userActivity);
        System.out.println("============================================");

        return userActivity;
    }
}
