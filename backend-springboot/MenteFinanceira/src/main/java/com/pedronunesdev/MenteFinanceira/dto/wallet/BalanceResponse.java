package com.pedronunesdev.MenteFinanceira.dto.wallet;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal balance
) {
}