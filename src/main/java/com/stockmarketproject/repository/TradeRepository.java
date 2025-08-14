package com.stockmarketproject.repository;

import com.stockmarketproject.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdAndTimestampBetween(Long userId, Instant from, Instant to);
    List<Trade> findByUserId(Long userId);
}