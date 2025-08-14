package com.stockmarketproject.controller;

import com.stockmarketproject.entity.SystemSettings;
import com.stockmarketproject.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class SystemBalanceController {

    private final SystemService systemService;

    @GetMapping("/balance")
    public ResponseEntity<SystemSettings> balance() {
        return ResponseEntity.ok(systemService.setCommission(systemService.commission())); // sadece mevcut kaydı döndürmek için küçük hile
    }
}
