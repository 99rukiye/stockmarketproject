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
    public void importAll() {
        try {
            List<ScrapedStock> all = scraper.fetchAll();
            int created = 0, updated = 0;

            for (ScrapedStock s : all) {
                Stock st = stockRepo.findBySymbol(s.symbol()).orElseGet(Stock::new);
                boolean isNew = (st.getId() == null);

                st.setSymbol(s.symbol());
                st.setName(s.name());
                st.setActive(true);
                if (st.getAvailableQuantity() == null || st.getAvailableQuantity() == 0) {
                    st.setAvailableQuantity(10_000L);
                }

                BigDecimal old = st.getLastPrice();
                BigDecimal now = (s.lastPrice() != null ? s.lastPrice() : BigDecimal.ZERO);
                st.setLastPrice(now);

                stockRepo.save(st);


                if (isNew || old == null || old.compareTo(now) != 0) {
                    StockPriceHistory h = new StockPriceHistory();
                    h.setStock(st);
                    h.setPrice(now);
                    h.setTimestamp(Instant.now());
                    historyRepo.save(h);
                }

                if (isNew) created++; else updated++;
            }

            log.info("Import finished. created={}, touched={}", created, updated);
        } catch (Exception ex) {
            log.error("Import failed", ex);
        }
    }
}
