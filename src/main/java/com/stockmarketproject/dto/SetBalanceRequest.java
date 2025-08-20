package com.stockmarketproject.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SetBalanceRequest(@NotNull BigDecimal balance) {}
