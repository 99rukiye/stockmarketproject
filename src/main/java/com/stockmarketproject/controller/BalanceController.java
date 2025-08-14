package com.stockmarketproject.controller;

import com.stockmarketproject.dto.TopUpRequest;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.service.TopUpCardService;
import com.stockmarketproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final TopUpCardService topUpCardService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> myBalance(Authentication auth) {
        User u = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(Map.of("email", u.getEmail(), "balance", u.getBalance()));
    }

    @PostMapping("/topup")
    public ResponseEntity<?> useTopUp(@Valid @RequestBody TopUpRequest req, Authentication auth) {
        Long userId = userService.getByEmail(auth.getName()).getId();
        topUpCardService.useCode(req.code(), userId);
        return ResponseEntity.ok(Map.of("message", "balance_updated"));
    }
}
