package com.stockmarketproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor
public class StockPriceHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant timestamp;

    public static StockPriceHistory of(Stock stock, BigDecimal price, Instant ts) {
        StockPriceHistory h = new StockPriceHistory();
        h.setStock(stock);
        h.setPrice(price);
        h.setTimestamp(ts);
        return h;
    }
}