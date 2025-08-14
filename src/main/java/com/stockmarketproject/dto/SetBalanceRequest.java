package com.stockmarketproject.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record SetBalanceRequest(@DecimalMin("0.00") BigDecimal amount) {}