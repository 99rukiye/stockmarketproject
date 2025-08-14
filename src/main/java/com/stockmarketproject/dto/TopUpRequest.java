package com.stockmarketproject.dto;

import jakarta.validation.constraints.NotBlank;

public record TopUpRequest(@NotBlank String code) {}