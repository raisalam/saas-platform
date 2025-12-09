package com.saas.platform.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_attribute",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "attribute_key"}))
public class UserAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attribute_key", nullable = false)
    private String key;

    @Column(name = "attribute_value", columnDefinition = "TEXT")
    private String value;

    // getters/setters
}
