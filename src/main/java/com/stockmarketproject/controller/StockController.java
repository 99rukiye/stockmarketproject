package com.stockmarketproject.controller;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.entity.StockPriceHistory;
import com.stockmarketproject.repository.StockPriceHistoryRepository;
import com.stockmarketproject.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockPriceHistoryRepository historyRepo;

    @GetMapping
    public ResponseEntity<List<Stock>> listActive() {
        return ResponseEntity.ok(stockService.listActive());
    }

    @GetMapping("/{symbol}/history")
    public ResponseEntity<List<StockPriceHistory>> history(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Long stockId = stockService.bySymbol(symbol).getId();
        return ResponseEntity.ok(historyRepo.findByStockIdAndTimestampBetween(stockId, from, to));
    }
}
