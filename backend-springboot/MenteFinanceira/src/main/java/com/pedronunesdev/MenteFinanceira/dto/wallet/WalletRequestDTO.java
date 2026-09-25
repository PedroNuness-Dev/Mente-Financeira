package com.pedronunesdev.MenteFinanceira.dto.wallet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WalletRequestDTO(
        @NotNull
        @Min(1)
        BigDecimal initialBalance
) {
}