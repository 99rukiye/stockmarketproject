package com.stockmarketproject.config;

import com.stockmarketproject.entity.Role;
import com.stockmarketproject.entity.SystemSettings;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.repository.SystemSettingsRepository;
import com.stockmarketproject.repository.UserRepository;
import com.stockmarketproject.service.MarketImportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SystemSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final MarketImportService importService;

    @Value("${app.seed.admin.email:admin@stock.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (settingsRepository.count() == 0) {
            SystemSettings s = new SystemSettings();
            s.setCommissionPercent(new BigDecimal("0.05"));
            s.setSystemBalance(BigDecimal.ZERO);
            settingsRepository.save(s);
            log.info("System settings initialized (commission=5%).");
        }

        userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setBalance(BigDecimal.ZERO);
            User saved = userRepository.save(admin);
            log.info("Seed admin created: {}", adminEmail);
            return saved;
        });

        importService.seedIfEmpty();
    }
}
