package com.stockmarketproject.repository;

import com.stockmarketproject.entity.TopUpCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopUpCardRepository extends JpaRepository<TopUpCard, Long> {
    Optional<TopUpCard> findByCode(String code);
}