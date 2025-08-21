package com.stockmarketproject.service;

import com.stockmarketproject.scrape.BigparaScraperService;
import com.stockmarketproject.scrape.ScrapedStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketPriceScheduler {

    private final BigparaScraperService scraper;
    private final PriceUpdater priceUpdater;

    @Scheduled(fixedDelayString = "${app.prices.refresh-ms:60000}")
    public void refreshPrices() {
        try {
            List<ScrapedStock> scraped = scraper.fetchAll();
            int updated = priceUpdater.apply(scraped);
            log.info("Scheduler: refresh done. updated={}", updated);
        } catch (Exception e) {
            log.warn("Scheduler: refresh failed: {}", e.toString());
        }
    }
}
