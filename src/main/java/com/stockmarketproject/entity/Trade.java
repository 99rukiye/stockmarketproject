package com.stockmarketproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trade", indexes = @Index(name="idx_trade_user_time", columnList = "user_id,timestamp"))
@Getter @Setter @NoArgsConstructor
public class Trade {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false) @JoinColumn(name = "stock_id")
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType type;               // BUY / SELL

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerShare;     // işlem anındaki birim fiyat

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commission;        // kesilen komisyon

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCost;         // BUY: ödenen toplam; SELL: elde edilen net

    @Column(nullable = false)
    private Instant timestamp;
}
