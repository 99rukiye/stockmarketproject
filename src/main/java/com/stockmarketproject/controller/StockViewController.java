package com.stockmarketproject.controller;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockViewController {

    private final StockRepository stockRepository;


    @GetMapping({"/", "/stocks"})
    public String stocks(Model model) {
        List<Stock> stocks = stockRepository.findAll().stream()
                .filter(Stock::isActive)
                .sorted(Comparator.comparing(Stock::getSymbol))
                .toList();

        model.addAttribute("stocks", stocks);
        model.addAttribute("count", stocks.size());
        return "stocks";
    }
}
