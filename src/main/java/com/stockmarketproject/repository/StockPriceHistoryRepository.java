package com.stockmarketproject.repository;

import com.stockmarketproject.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {
    List<StockPriceHistory> findByStockIdAndTimestampBetween(Long stockId, Instant from, Instant to);
}