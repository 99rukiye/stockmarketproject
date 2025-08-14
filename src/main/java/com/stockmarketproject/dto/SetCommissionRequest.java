package com.stockmarketproject.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record SetCommissionRequest(@DecimalMin("0.00") @DecimalMax("1.00") BigDecimal commissionPercent) {}