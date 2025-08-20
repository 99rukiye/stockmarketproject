package com.stockmarketproject.dto;

import com.stockmarketproject.entity.Stock;
import java.math.BigDecimal;

public record StockDto(
        Long id,
        String symbol,
        String name,
        BigDecimal lastPrice,
        long availableQuantity,
        boolean active
) {
    public static StockDto from(Stock s) {
        return new StockDto(
                s.getId(),
                s.getSymbol(),
                s.getName(),
                s.getLastPrice(),
                s.getAvailableQuantity(),
                s.isActive()
        );
    }
}
