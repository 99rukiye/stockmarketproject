package com.stockmarketproject.service;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.entity.StockPriceHistory;
import com.stockmarketproject.repository.StockPriceHistoryRepository;
import com.stockmarketproject.repository.StockRepository;
import com.stockmarketproject.scrape.BigparaScraperService;
import com.stockmarketproject.scrape.ScrapedStock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketImportService {

    private final BigparaScraperService scraper;
    private final StockRepository stockRepo;
    private final StockPriceHistoryRepository historyRepo;

    @Transactional
    public void seedIfEmpty() {
        if (stockRepo.count() > 0) {
            log.info("Stocks already present, seed skipped.");
            return;
        }
        importAll();
    }

    @Transactional
    public void importAll() {
        List<ScrapedStock> all;
        try {
            all = scraper.fetchAll();
        } catch (IOException e) {
            log.error("ImportAll: Bigpara'dan veri alınamadı: {}", e.toString(), e);
            return;
        }

        int merged = 0;
        for (ScrapedStock s : all) {
            if (s == null || s.symbol() == null) continue;

            Stock st = stockRepo.findBySymbol(s.symbol()).orElseGet(Stock::new);
            boolean isNew = (st.getId() == null);

            st.setSymbol(s.symbol());
            st.setName(s.name() != null ? s.name() : s.symbol());
            st.setActive(true);
            if (st.getAvailableQuantity() == null) st.setAvailableQuantity(10_000L);
            if (s.lastPrice() != null) st.setLastPrice(s.lastPrice());

            Stock saved = stockRepo.save(st);

            if (isNew) {
                StockPriceHistory h = new StockPriceHistory();
                h.setStock(saved);
                h.setPrice(saved.getLastPrice() != null ? saved.getLastPrice() : BigDecimal.ZERO);
                h.setTimestamp(Instant.now());
                historyRepo.save(h);
            }
            merged++;
        }
        log.info("ImportAll done. saved/merged={}", merged);
    }
}
