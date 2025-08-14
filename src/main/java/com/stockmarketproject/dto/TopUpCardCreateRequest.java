package com.stockmarketproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TopUpCardCreateRequest(@NotBlank String code,
                                     @Positive BigDecimal amount) {}