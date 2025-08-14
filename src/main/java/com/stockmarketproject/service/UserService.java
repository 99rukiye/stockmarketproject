package com.stockmarketproject.service;

import com.stockmarketproject.dto.CreateUserRequest;
import com.stockmarketproject.dto.SetBalanceRequest;
import com.stockmarketproject.dto.UpdateRoleRequest;
import com.stockmarketproject.entity.Portfolio;
import com.stockmarketproject.entity.Role;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.exception.NotFoundException;
import com.stockmarketproject.repository.PortfolioRepository;
import com.stockmarketproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PortfolioRepository portfolioRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public User register(String email, String rawPassword) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(Role.USER);
        u.setEnabled(true);
        u.setBalance(BigDecimal.ZERO);
        userRepo.save(u);


        Portfolio p = new Portfolio();
        p.setUser(u);
        portfolioRepo.save(p);
        return u;
    }

    @Transactional
    public User createByAdmin(CreateUserRequest req) {
        User u = new User();
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.valueOf(req.role().toUpperCase()));
        u.setEnabled(true);
        u.setBalance(BigDecimal.ZERO);
        userRepo.save(u);

        if (u.getRole() == Role.USER) {
            Portfolio p = new Portfolio();
            p.setUser(u);
            portfolioRepo.save(p);
        }
        return u;
    }

    @Transactional
    public User updateRole(Long userId, UpdateRoleRequest req) {
        User u = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        u.setRole(Role.valueOf(req.role().toUpperCase()));
        return u;
    }

    @Transactional
    public User setBalance(Long userId, SetBalanceRequest req) {
        User u = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        u.setBalance(req.amount());
        return u;
    }

    public User getByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
