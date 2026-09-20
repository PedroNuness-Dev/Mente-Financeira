package com.pedronunesdev.MenteFinanceira.dto.carteira;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositoCarteiraRequest(
        @NotNull
        @Min(1)
        BigDecimal valorDeposito,
        @NotBlank
        String categoriaMovimentacao
) {
}
