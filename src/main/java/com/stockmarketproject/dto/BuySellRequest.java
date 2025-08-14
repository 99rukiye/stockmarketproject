package com.stockmarketproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record BuySellRequest(@NotBlank String symbol,
                             @Positive long quantity) {}