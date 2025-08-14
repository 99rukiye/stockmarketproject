package com.stockmarketproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@Email @NotBlank String email,
                                @NotBlank String password,
                                @NotBlank String role) {}