package com.stockmarketproject.service;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.repository.StockRepository;
import com.stockmarketproject.scrape.BigparaScraperService;
import com.stockmarketproject.scrape.ScrapedStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketImportService {

    private final BigparaScraperService scraper;
    private final StockRepository stockRepo;

    @Transactional
    public int importMissingFromSource() {
        List<ScrapedStock> all = scraper.fetchAll();
        int created = 0;
        for (ScrapedStock s : all) {
            if (stockRepo.findBySymbol(s.symbol()).isEmpty()) {
                Stock st = new Stock();
                st.setSymbol(s.symbol());
                st.setName(s.name());
                st.setActive(true);
                st.setAvailableQuantity(10_000L);
                if (s.price() != null) st.setLastPrice(s.price().setScale(2, RoundingMode.HALF_UP));
                stockRepo.save(st);
                created++;
            }
        }
        log.info("MarketImport: {} new stocks created.", created);
        return created;
    }
}
