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

    @GetMapping("/stocks")
    public String stocksPage(Model model) {
        List<Stock> list = stockRepository.findAll().stream()
                .sorted(Comparator.comparing(Stock::getSymbol, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("stocks", list);
        model.addAttribute("count", list.size());
        return "stocks";
    }
}
