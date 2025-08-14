package com.stockmarketproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock", indexes = {
        @Index(name = "idx_stock_symbol", columnList = "symbol", unique = true)
})
@Getter @Setter @NoArgsConstructor
public class Stock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String symbol;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    // Sistem envanterindeki hisse adedi (opsiyonel)
    @Column(nullable = false)
    private Long availableQuantity = 0L;

    // Son işlem/güncelleme fiyatı
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lastPrice = BigDecimal.ONE; // 1.00 default
}
