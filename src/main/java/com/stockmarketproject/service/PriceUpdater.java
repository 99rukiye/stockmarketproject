package com.stockmarketproject.service;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.entity.StockPriceHistory;
import com.stockmarketproject.repository.StockPriceHistoryRepository;
import com.stockmarketproject.repository.StockRepository;
import com.stockmarketproject.scrape.ScrapedStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceUpdater {

    private final StockRepository stockRepo;
    private final StockPriceHistoryRepository historyRepo;

    @Transactional
    public int apply(List<ScrapedStock> scraped) {
        if (scraped == null || scraped.isEmpty()) return 0;

        Map<String, ScrapedStock> map =
                scraped.stream().collect(Collectors.toMap(ScrapedStock::symbol, s -> s, (a, b) -> a));

        List<Stock> stocks = stockRepo.findAll();
        int updated = 0;
        Instant now = Instant.now();

        for (Stock st : stocks) {
            ScrapedStock s = map.get(st.getSymbol());
            if (s == null || s.price() == null) continue;

            BigDecimal newPrice = s.price().setScale(2, RoundingMode.HALF_UP);
            if (st.getLastPrice() == null || st.getLastPrice().compareTo(newPrice) != 0) {
                st.setLastPrice(newPrice);
                stockRepo.save(st);

                StockPriceHistory h = new StockPriceHistory();
                h.setStock(st);
                h.setPrice(newPrice);
                h.setTimestamp(now);
                historyRepo.save(h);

                updated++;
            }
        }
        log.info("PriceUpdater: {} stock updated.", updated);
        return updated;
    }
}
