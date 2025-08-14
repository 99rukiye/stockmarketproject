package com.stockmarketproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id","stock_id"}))
@Getter @Setter @NoArgsConstructor
public class PortfolioItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @ManyToOne(optional = false) @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(nullable = false)
    private Long quantity = 0L;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal avgPrice = BigDecimal.ZERO; // maliyet ortalaması
}
