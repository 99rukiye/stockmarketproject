package com.stockmarketproject.controller;

import com.stockmarketproject.dto.BuySellRequest;
import com.stockmarketproject.entity.Trade;
import com.stockmarketproject.service.TradeService;
import com.stockmarketproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final UserService userService;

    @GetMapping("/my")
    public ResponseEntity<List<Trade>> myTrades(
            Authentication auth,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        Long userId = userService.getByEmail(auth.getName()).getId();
        return ResponseEntity.ok(tradeService.myTrades(userId, from, to));
    }

    @PostMapping("/buy")
    public ResponseEntity<Trade> buy(@Valid @RequestBody BuySellRequest req, Authentication auth) {
        Long userId = userService.getByEmail(auth.getName()).getId();
        return ResponseEntity.ok(tradeService.buy(userId, req));
    }

    @PostMapping("/sell")
    public ResponseEntity<Trade> sell(@Valid @RequestBody BuySellRequest req, Authentication auth) {
        Long userId = userService.getByEmail(auth.getName()).getId();
        return ResponseEntity.ok(tradeService.sell(userId, req));
    }
}
