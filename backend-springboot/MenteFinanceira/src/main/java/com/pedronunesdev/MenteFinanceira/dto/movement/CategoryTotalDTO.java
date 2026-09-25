package com.pedronunesdev.MenteFinanceira.dto.movement;

import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;

import java.math.BigDecimal;

public record CategoryTotalDTO(
        MovementCategory category,
        MovementType movementType,
        BigDecimal totalAmount
) {
}