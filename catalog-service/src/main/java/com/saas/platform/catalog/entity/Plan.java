package com.saas.platform.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@ToString
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false, unique = true)
    private String planId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    private String title;
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    private Double price;

    @Column(name = "discount_price")
    private Double discountPrice;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    private boolean enabled;

    private boolean popular;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
