package com.stockmarketproject.service;

import com.stockmarketproject.dto.CreateUserRequest;
import com.stockmarketproject.dto.SetBalanceRequest;
import com.stockmarketproject.dto.UpdateRoleRequest;
import com.stockmarketproject.entity.Portfolio;
import com.stockmarketproject.entity.Role;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.exception.BadRequestException;
import com.stockmarketproject.exception.NotFoundException;
import com.stockmarketproject.repository.PortfolioRepository;
import com.stockmarketproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PortfolioRepository portfolioRepo;
    private final PasswordEncoder passwordEncoder;


    public User getByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    public User getById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
    }

    private void ensureEmailNotExists(String email) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email already exists: " + email);
        }
    }

    private static Role toRole(String roleText) {
        try {
            return Role.valueOf(roleText.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid role: " + roleText + " (allowed: USER, ADMIN)");
        }
    }

    private static BigDecimal twoScale(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val.setScale(2, RoundingMode.HALF_UP);
    }

    private void createPortfolioIfMissing(User u) {
        if (portfolioRepo.findByUserId(u.getId()).isEmpty()) {
            Portfolio p = new Portfolio();
            p.setUser(u);
            portfolioRepo.save(p);
        }
    }
    @Transactional
    public User register(String email, String rawPassword) {
        ensureEmailNotExists(email);
        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(Role.USER);
        u.setEnabled(true);
        u.setBalance(BigDecimal.ZERO);
        u = userRepo.save(u);
        createPortfolioIfMissing(u);
        return u;
    }
    @Transactional
    public User createByAdmin(CreateUserRequest req) {
        ensureEmailNotExists(req.email());
        User u = new User();
        u.setEmail(req.email().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRole(toRole(req.role()));
        u.setEnabled(true);
        u.setBalance(BigDecimal.ZERO);
        u = userRepo.save(u);
        createPortfolioIfMissing(u);
        return u;
    }
    @Transactional
    public User updateRole(Long userId, UpdateRoleRequest req) {
        User u = getById(userId);
        u.setRole(toRole(req.role()));
        return u;
    }
    @Transactional
    public User setBalance(Long userId, SetBalanceRequest req) {
        User u = getById(userId);
        u.setBalance(twoScale(req.balance()));
        return u;
    }
}
