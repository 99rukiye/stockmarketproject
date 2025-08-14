package com.stockmarketproject.repository;

import com.stockmarketproject.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    Optional<PortfolioItem> findByPortfolioIdAndStockId(Long portfolioId, Long stockId);
}