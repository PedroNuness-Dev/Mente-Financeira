package com.pedronunesdev.MenteFinanceira.dto.wallet;

import com.pedronunesdev.MenteFinanceira.dto.movement.MovementDTOResponse;

import java.math.BigDecimal;

public record WalletResponseDTO(
        Long id,
        BigDecimal balance,
        MovementDTOResponse movement
) {
}