package com.stockmarketproject.config;

import com.stockmarketproject.entity.*;
import com.stockmarketproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository historyRepository;
    private final SystemSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (settingsRepository.findAll().isEmpty()) {
            SystemSettings s = new SystemSettings();
            s.setCommissionPercent(new BigDecimal("0.05")); // %5
            s.setSystemBalance(BigDecimal.ZERO);
            settingsRepository.save(s);
        }


        String adminEmail = "admin@stock.local";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setBalance(BigDecimal.ZERO);
            userRepository.save(admin);
        }


        if (stockRepository.count() == 0) {
            Stock s1 = new Stock();
            s1.setSymbol("ASELS");
            s1.setName("Aselsan");
            s1.setActive(true);
            s1.setAvailableQuantity(10_000L);
            s1.setLastPrice(new BigDecimal("100.00"));

            Stock s2 = new Stock();
            s2.setSymbol("THYAO");
            s2.setName("Türk Hava Yolları");
            s2.setActive(true);
            s2.setAvailableQuantity(10_000L);
            s2.setLastPrice(new BigDecimal("150.00"));

            Stock s3 = new Stock();
            s3.setSymbol("BIMAS");
            s3.setName("BIM");
            s3.setActive(true);
            s3.setAvailableQuantity(10_000L);
            s3.setLastPrice(new BigDecimal("75.00"));

            List<Stock> saved = stockRepository.saveAll(List.of(s1, s2, s3));


            Instant now = Instant.now();
            for (Stock st : saved) {
                StockPriceHistory h = new StockPriceHistory();
                h.setStock(st);
                h.setPrice(st.getLastPrice());
                h.setTimestamp(now);
                historyRepository.save(h);
            }
        }
    }
}
