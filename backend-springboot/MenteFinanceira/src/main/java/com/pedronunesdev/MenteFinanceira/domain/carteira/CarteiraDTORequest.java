package com.pedronunesdev.MenteFinanceira.domain.carteira;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CarteiraDTORequest(
        @NotNull
        @Min(1)
        BigDecimal saldoInicial
) {
}
