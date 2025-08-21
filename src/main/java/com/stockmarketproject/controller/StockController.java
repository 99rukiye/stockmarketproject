package com.stockmarketproject.controller;

import com.stockmarketproject.dto.StockDto;
import com.stockmarketproject.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;

    @GetMapping
    public ResponseEntity<List<StockDto>> all() {
        List<StockDto> list = stockRepository
                .findAll(Sort.by(Sort.Direction.ASC, "symbol"))
                .stream()
                .map(StockDto::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("count", stockRepository.count());
    }
}
