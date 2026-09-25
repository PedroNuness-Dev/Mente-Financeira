package com.pedronunesdev.MenteFinanceira.dto.movement;

import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;

import java.math.BigDecimal;

public record MovementCategoryPercentageDTOResponse(
        MovementCategory category,
        MovementType movementType,
        BigDecimal totalAmount,
        BigDecimal percentage
) {}