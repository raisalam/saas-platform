package com.saas.platform.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)

    @Column(name = "activity_type")
    private ActivityType activityType;

    private String title;
    private String message;

    private Double amount;

    @Column(name = "balance_after")
    private Double balanceAfter;

    @Column(columnDefinition = "json")
    private String metadata;

    @Column(name = "correlation_id")
    private String correlationId;

    @Builder.Default
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
