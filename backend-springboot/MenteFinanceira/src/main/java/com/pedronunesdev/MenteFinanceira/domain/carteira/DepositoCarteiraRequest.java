package com.pedronunesdev.MenteFinanceira.domain.carteira;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositoCarteiraRequest(
        @NotNull
        @Min(1)
        BigDecimal valorDeposito,
        String categoriaMovimentacao
) {
}
