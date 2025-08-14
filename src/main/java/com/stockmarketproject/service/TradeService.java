package com.stockmarketproject.service;

import com.stockmarketproject.dto.BuySellRequest;
import com.stockmarketproject.entity.*;
import com.stockmarketproject.exception.BadRequestException;
import com.stockmarketproject.exception.NotFoundException;
import com.stockmarketproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final UserRepository userRepo;
    private final StockRepository stockRepo;
    private final PortfolioRepository portfolioRepo;
    private final PortfolioItemRepository itemRepo;
    private final TradeRepository tradeRepo;
    private final SystemService systemService;

    private BigDecimal two(BigDecimal x) { return x.setScale(2, RoundingMode.HALF_UP); }

    private Portfolio ensurePortfolio(User u) {
        return portfolioRepo.findByUserId(u.getId()).orElseGet(() -> {
            Portfolio p = new Portfolio();
            p.setUser(u);
            return portfolioRepo.save(p);
        });
    }

    public List<Trade> myTrades(Long userId) { return tradeRepo.findByUserId(userId); }
    public List<Trade> myTrades(Long userId, Instant from, Instant to) {
        if (from == null || to == null) return myTrades(userId);
        return tradeRepo.findByUserIdAndTimestampBetween(userId, from, to);
    }

    @Transactional
    public Trade buy(Long userId, BuySellRequest req) {
        User u = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Stock s = stockRepo.findBySymbol(req.symbol().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Stock not found"));
        if (!s.isActive()) throw new BadRequestException("Stock not active");

        // Sistem envanteri kontrolü
        long qty = req.quantity();
        if (s.getAvailableQuantity() < qty) throw new BadRequestException("Not enough stock in system");

        BigDecimal price = s.getLastPrice();
        BigDecimal gross = price.multiply(BigDecimal.valueOf(qty));
        BigDecimal commission = two(gross.multiply(systemService.commission()));
        BigDecimal total = two(gross.add(commission));
        if (u.getBalance().compareTo(total) < 0) throw new BadRequestException("Insufficient balance");

        // Bakiyeler / komisyon / envanter
        u.setBalance(u.getBalance().subtract(total));
        s.setAvailableQuantity(s.getAvailableQuantity() - qty);
        systemService.addSystemIncome(commission);

        // Portföy
        Portfolio p = ensurePortfolio(u);
        PortfolioItem item = itemRepo.findByPortfolioIdAndStockId(p.getId(), s.getId()).orElse(null);
        if (item == null) {
            item = new PortfolioItem();
            item.setPortfolio(p);
            item.setStock(s);
            item.setQuantity(qty);
            item.setAvgPrice(price);
            itemRepo.save(item);
        } else {
            long oldQty = item.getQuantity();
            long newQty = oldQty + qty;
            BigDecimal newAvg = two(
                    item.getAvgPrice().multiply(BigDecimal.valueOf(oldQty))
                            .add(price.multiply(BigDecimal.valueOf(qty)))
                            .divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP)
            );
            item.setQuantity(newQty);
            item.setAvgPrice(newAvg);
        }

        // Trade kaydı
        Trade t = new Trade();
        t.setUser(u);
        t.setStock(s);
        t.setType(TradeType.BUY);
        t.setQuantity(qty);
        t.setPricePerShare(price);
        t.setCommission(commission);
        t.setTotalCost(total);
        t.setTimestamp(Instant.now());
        return tradeRepo.save(t);
    }

    @Transactional
    public Trade sell(Long userId, BuySellRequest req) {
        User u = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Stock s = stockRepo.findBySymbol(req.symbol().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Stock not found"));
        if (!s.isActive()) throw new BadRequestException("Stock not active");

        long qty = req.quantity();

        Portfolio p = ensurePortfolio(u);
        PortfolioItem item = itemRepo.findByPortfolioIdAndStockId(p.getId(), s.getId())
                .orElseThrow(() -> new BadRequestException("No position to sell"));
        if (item.getQuantity() < qty) throw new BadRequestException("Insufficient quantity");

        BigDecimal price = s.getLastPrice();
        BigDecimal gross = price.multiply(BigDecimal.valueOf(qty));
        BigDecimal commission = two(gross.multiply(systemService.commission()));
        BigDecimal net = two(gross.subtract(commission));

        // Portföy ve sistem envanteri güncelle
        item.setQuantity(item.getQuantity() - qty);
        if (item.getQuantity() == 0) item.setAvgPrice(BigDecimal.ZERO);
        s.setAvailableQuantity(s.getAvailableQuantity() + qty);

        // Bakiyeler / komisyon
        u.setBalance(u.getBalance().add(net));
        systemService.addSystemIncome(commission);

        // Trade kaydı
        Trade t = new Trade();
        t.setUser(u);
        t.setStock(s);
        t.setType(TradeType.SELL);
        t.setQuantity(qty);
        t.setPricePerShare(price);
        t.setCommission(commission);
        t.setTotalCost(net);
        t.setTimestamp(Instant.now());
        return tradeRepo.save(t);
    }
}
