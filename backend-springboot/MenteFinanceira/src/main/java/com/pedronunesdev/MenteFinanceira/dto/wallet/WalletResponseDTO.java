package com.pedronunesdev.MenteFinanceira.dto.wallet;

import com.pedronunesdev.MenteFinanceira.dto.movement.MovementResponse;

import java.math.BigDecimal;

public record WalletResponseDTO(
        Long id,
        BigDecimal balance,
        MovementResponse movement
) {
}