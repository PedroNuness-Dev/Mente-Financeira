package com.pedronunesdev.MenteFinanceira.dto.wallet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WalletDepositRequest(
        @NotNull
        @Min(1)
        BigDecimal depositAmount,
        @NotBlank
        String movementCategory,
        @NotBlank
        String description
) {
}