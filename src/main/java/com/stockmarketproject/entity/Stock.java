package com.stockmarketproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "stock", indexes = {
        @Index(name = "idx_stock_symbol", columnList = "symbol", unique = true)
})
@Getter @Setter @NoArgsConstructor
public class Stock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 12)
    private String symbol;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Long availableQuantity = 10_000L;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lastPrice = BigDecimal.ZERO;
}
