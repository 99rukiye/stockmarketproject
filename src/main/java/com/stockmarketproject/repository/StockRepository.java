package com.stockmarketproject.repository;

import com.stockmarketproject.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findAllByActiveTrueOrderBySymbolAsc();

    List<Stock> findAllByOrderBySymbolAsc();
}
