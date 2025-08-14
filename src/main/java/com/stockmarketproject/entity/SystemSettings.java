package com.stockmarketproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor
public class SystemSettings {
    @Id
    private Long id = 1L; // singleton

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionPercent;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal systemBalance = BigDecimal.ZERO;
}