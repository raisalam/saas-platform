package com.saas.platform.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private String title;
    private String message;

    private Double amount;
    private Double balanceAfter;

    @Column(columnDefinition = "json")
    private String metadata;

    private String correlationId;

    private LocalDateTime createdAt;
}
