package com.stockmarketproject.controller;

import com.stockmarketproject.dto.*;
import com.stockmarketproject.entity.Stock;
import com.stockmarketproject.entity.SystemSettings;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.service.StockService;
import com.stockmarketproject.service.SystemService;
import com.stockmarketproject.service.TopUpCardService;
import com.stockmarketproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final TopUpCardService topUpCardService;
    private final StockService stockService;
    private final SystemService systemService;

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userService.createByAdmin(req));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<User> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest req) {
        return ResponseEntity.ok(userService.updateRole(id, req));
    }

    @PatchMapping("/users/{id}/balance")
    public ResponseEntity<User> setBalance(@PathVariable Long id, @Valid @RequestBody SetBalanceRequest req) {
        return ResponseEntity.ok(userService.setBalance(id, req));
    }

    @PostMapping("/topup-cards")
    public ResponseEntity<?> createTopUpCard(@Valid @RequestBody TopUpCardCreateRequest req,
                                             Authentication auth) {
        Long adminId = userService.getByEmail(auth.getName()).getId();
        return ResponseEntity.ok(topUpCardService.create(req, adminId));
    }

    @PostMapping("/stocks")
    public ResponseEntity<Stock> addStock(@RequestParam String symbol,
                                          @RequestParam String name,
                                          @RequestParam BigDecimal price,
                                          @RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(stockService.addStock(symbol, name, price, active));
    }

    @PatchMapping("/stocks/{id}/active")
    public ResponseEntity<Stock> setActive(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(stockService.setActive(id, active));
    }

    @PatchMapping("/settings/commission")
    public ResponseEntity<SystemSettings> setCommission(@Valid @RequestBody SetCommissionRequest req) {
        return ResponseEntity.ok(systemService.setCommission(req.commissionPercent()));
    }
}
