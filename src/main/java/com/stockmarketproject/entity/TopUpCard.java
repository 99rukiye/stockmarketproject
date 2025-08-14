package com.stockmarketproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "top_up_card", indexes = {
        @Index(name = "idx_topup_code", columnList = "code", unique = true)
})
public class TopUpCard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean used = false;


    private Long createdByUserId;
    private Long usedByUserId;
    private Instant usedAt;
}
