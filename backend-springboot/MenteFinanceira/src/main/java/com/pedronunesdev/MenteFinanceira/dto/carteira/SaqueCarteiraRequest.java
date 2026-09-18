package com.pedronunesdev.MenteFinanceira.dto.carteira;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SaqueCarteiraRequest(
        @NotNull
        @Min(1)
        BigDecimal valorSaque,
        String categoriaMovimentacao
) {
}
