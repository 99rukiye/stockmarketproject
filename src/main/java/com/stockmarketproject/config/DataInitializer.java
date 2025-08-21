package com.stockmarketproject.config;

import com.stockmarketproject.entity.Role;
import com.stockmarketproject.entity.SystemSettings;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.repository.SystemSettingsRepository;
import com.stockmarketproject.repository.UserRepository;
import com.stockmarketproject.service.MarketImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SystemSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final MarketImportService importService;

    @Override
    public void run(String... args) {
        if (settingsRepository.findAll().isEmpty()) {
            SystemSettings s = new SystemSettings();
            s.setCommissionPercent(new BigDecimal("0.05"));
            s.setSystemBalance(BigDecimal.ZERO);
            settingsRepository.save(s);
        }

        userRepository.findByEmail("admin@stock.local").orElseGet(() -> {
            User admin = new User();
            admin.setEmail("admin@stock.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setBalance(BigDecimal.ZERO);
            return userRepository.save(admin);
        });
        importService.importAll();
    }
}
