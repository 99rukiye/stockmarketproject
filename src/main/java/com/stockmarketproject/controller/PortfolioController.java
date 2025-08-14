package com.stockmarketproject.controller;

import com.stockmarketproject.entity.Portfolio;
import com.stockmarketproject.service.UserService;
import com.stockmarketproject.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioRepository portfolioRepo;
    private final UserService userService;

    @GetMapping("/my")
    public ResponseEntity<Portfolio> myPortfolio(Authentication auth) {
        Long userId = userService.getByEmail(auth.getName()).getId();
        Portfolio p = portfolioRepo.findByUserId(userId).orElseGet(() -> {
            var np = new Portfolio();
            np.setUser(userService.getById(userId));
            return portfolioRepo.save(np);
        });
        return ResponseEntity.ok(p);
    }
}
