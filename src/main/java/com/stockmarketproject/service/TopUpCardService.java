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


    @Transactional
    public TopUpCard create(TopUpCardCreateRequest req, Long createdByUserId) {

        cardRepo.findByCode(req.code()).ifPresent(c -> {
            throw new BadRequestException("Code already exists");
        });

        TopUpCard c = new TopUpCard();
        c.setCode(req.code());
        c.setAmount(req.amount());
        c.setUsed(false);
        c.setCreatedByUserId(createdByUserId);
        return cardRepo.save(c);
    }


    @Transactional
    public void useCode(String code, Long userId) {
        TopUpCard c = cardRepo.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Code not found"));

        if (c.isUsed()) {
            throw new BadRequestException("Code already used");
        }

        User u = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));


        u.setBalance(u.getBalance().add(c.getAmount()));


        c.setUsed(true);
        c.setUsedByUserId(userId);
        c.setUsedAt(Instant.now());

    }
}
