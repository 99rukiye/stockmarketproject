// src/main/java/com/stockmarketproject/controller/BalanceController.java
package com.stockmarketproject.controller;

import com.stockmarketproject.dto.TopUpRequest;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.service.TopUpCardService;
import com.stockmarketproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final UserService userService;
    private final TopUpCardService topUpCardService;

    @GetMapping("/me")
    public Map<String, BigDecimal> myBalance(
            @AuthenticationPrincipal(expression = "name") String email) {
        User u = userService.getByEmail(email);
        return Map.of("balance", u.getBalance());
    }

    @PostMapping("/topup")
    public Map<String, BigDecimal> topup(
            @RequestBody TopUpRequest req,
            @AuthenticationPrincipal(expression = "name") String email) {

        User me = userService.getByEmail(email);

        // Top-up kodunu kullan (void döner)
        topUpCardService.useCode(req.code(), me.getId());

        // Güncellenen bakiyeyi tekrar oku
        User updated = userService.getByEmail(email);
        return Map.of("balance", updated.getBalance());
    }
}
