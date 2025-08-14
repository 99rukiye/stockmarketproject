package com.stockmarketproject.service;

import com.stockmarketproject.dto.TopUpCardCreateRequest;
import com.stockmarketproject.entity.TopUpCard;
import com.stockmarketproject.entity.User;
import com.stockmarketproject.exception.BadRequestException;
import com.stockmarketproject.exception.NotFoundException;
import com.stockmarketproject.repository.TopUpCardRepository;
import com.stockmarketproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TopUpCardService {

    private final TopUpCardRepository cardRepo;
    private final UserRepository userRepo;

    /**
     * Admin tarafından tek kullanımlık bakiye kodu oluşturur.
     */
    @Transactional
    public TopUpCard create(TopUpCardCreateRequest req, Long createdByUserId) {
        // Aynı kod varsa hata ver
        cardRepo.findByCode(req.code()).ifPresent(c -> {
            throw new BadRequestException("Code already exists");
        });

        TopUpCard c = new TopUpCard();
        c.setCode(req.code());
        c.setAmount(req.amount());
        c.setUsed(false);
        c.setCreatedByUserId(createdByUserId); // audit
        return cardRepo.save(c);
    }

    /**
     * Kullanıcı, kodu kullanarak bakiyesini artırır.
     */
    @Transactional
    public void useCode(String code, Long userId) {
        TopUpCard c = cardRepo.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Code not found"));

        if (c.isUsed()) {
            throw new BadRequestException("Code already used");
        }

        User u = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kullanıcının bakiyesine ekle
        u.setBalance(u.getBalance().add(c.getAmount()));

        // Kodun durumunu güncelle (audit bilgisiyle birlikte)
        c.setUsed(true);
        c.setUsedByUserId(userId);
        c.setUsedAt(Instant.now());
        // JPA @Transactional sayesinde değişiklikler commit edilir
    }
}
