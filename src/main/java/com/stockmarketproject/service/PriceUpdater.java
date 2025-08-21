package com.stockmarketproject.service;

import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.entity.StockPriceHistory;
import com.stockmarketproject.repository.StockPriceHistoryRepository;
import com.stockmarketproject.repository.StockRepository;
import com.stockmarketproject.scrape.ScrapedStock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceUpdater {

    private final StockRepository stockRepo;
    private final StockPriceHistoryRepository historyRepo;

    /**
     * Scraper'dan gelen son fiyatları DB'ye uygular.
     * Fiyat değiştiyse STOCK.last_price güncellenir ve STOCK_PRICE_HISTORY'ye satır eklenir.
     * @return güncellenen hisse sayısı
     */
    @Transactional
    public int apply(List<ScrapedStock> latest) {
        if (latest == null || latest.isEmpty()) return 0;


        Map<String, ScrapedStock> bySymbol = latest.stream()
                .filter(s -> s != null && s.symbol() != null && s.lastPrice() != null)
                .collect(toMap(
                        s -> s.symbol().trim().toUpperCase(),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<Stock> all = stockRepo.findAll();
        Instant now = Instant.now();
        int updated = 0;

        for (Stock st : all) {
            String sym = st.getSymbol() == null ? null : st.getSymbol().trim().toUpperCase();
            if (sym == null) continue;

            ScrapedStock web = bySymbol.get(sym);
            if (web == null || web.lastPrice() == null) continue;


            if (st.getLastPrice() == null || st.getLastPrice().compareTo(web.lastPrice()) != 0) {
                st.setLastPrice(web.lastPrice());
                stockRepo.save(st);

                StockPriceHistory h = new StockPriceHistory();
                h.setStock(st);
                h.setPrice(web.lastPrice());
                h.setTimestamp(now);
                historyRepo.save(h);

                updated++;
            }
        }

        log.debug("Price apply finished. updated={}", updated);
        return updated;
    }
}
