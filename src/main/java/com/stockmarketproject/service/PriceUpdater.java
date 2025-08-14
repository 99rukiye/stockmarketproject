package com.stockmarketproject.service;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.entity.StockPriceHistory;
import com.stockmarketproject.repository.StockPriceHistoryRepository;
import com.stockmarketproject.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PriceUpdater {

    private final StockRepository stockRepo;
    private final StockPriceHistoryRepository historyRepo;


    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void updateAllPrices() {
        List<Stock> all = stockRepo.findAll();
        for (Stock s : all) {
            if (!s.isActive()) continue;


            BigDecimal oldPrice = s.getLastPrice();
            if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) <= 0) {
                oldPrice = new BigDecimal("1.00");
            }


            double delta = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
            BigDecimal factor = BigDecimal.valueOf(1.0 + delta);

            BigDecimal newPrice = oldPrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            if (newPrice.compareTo(new BigDecimal("1.00")) < 0) {
                newPrice = new BigDecimal("1.00");
            }


            s.setLastPrice(newPrice);


            StockPriceHistory h = new StockPriceHistory();
            h.setStock(s);
            h.setPrice(newPrice);
            h.setTimestamp(Instant.now());
            historyRepo.save(h);
        }
    }
}
