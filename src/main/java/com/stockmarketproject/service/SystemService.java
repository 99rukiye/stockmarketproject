package com.stockmarketproject.service;

import com.stockmarketproject.entity.SystemSettings;
import com.stockmarketproject.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final SystemSettingsRepository settingsRepo;

    private SystemSettings ensure() {
        return settingsRepo.findAll().stream().findFirst().orElseGet(() -> {
            SystemSettings s = new SystemSettings();
            s.setCommissionPercent(new BigDecimal("0.0025")); // %0.25
            s.setSystemBalance(BigDecimal.ZERO);
            return settingsRepo.save(s);
        });
    }

    public BigDecimal commission() { return ensure().getCommissionPercent(); }

    @Transactional
    public SystemSettings setCommission(BigDecimal percent) {
        SystemSettings s = ensure();
        s.setCommissionPercent(percent);
        return s;
    }

    @Transactional
    public void addSystemIncome(BigDecimal amount) {
        SystemSettings s = ensure();
        s.setSystemBalance(s.getSystemBalance().add(amount));
    }
}
