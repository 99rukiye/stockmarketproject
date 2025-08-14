package com.stockmarketproject.service;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.exception.NotFoundException;
import com.stockmarketproject.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepo;

    public List<Stock> listActive() {
        return stockRepo.findAll().stream().filter(Stock::isActive).toList();
    }

    public Stock bySymbol(String symbol) {
        return stockRepo.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Stock not found"));
    }

    @Transactional
    public Stock addStock(String symbol, String name, BigDecimal price, boolean active) {
        Stock s = new Stock();
        s.setSymbol(symbol.toUpperCase());
        s.setName(name);
        s.setLastPrice(price);
        s.setActive(active);
        return stockRepo.save(s);
    }

    @Transactional
    public Stock setActive(Long id, boolean active) {
        Stock s = stockRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock not found"));
        s.setActive(active);
        return s;
    }

    @Transactional
    public Stock updatePrice(Long id, BigDecimal price) {
        Stock s = stockRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock not found"));
        s.setLastPrice(price);
        return s;
    }
}
