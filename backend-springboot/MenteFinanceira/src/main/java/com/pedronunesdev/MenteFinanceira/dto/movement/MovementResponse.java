package com.pedronunesdev.MenteFinanceira.dto.movement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovementResponse(
        Long movementId,
        BigDecimal movedAmount,
        LocalDateTime executionDate,
        MovementType movementType,
        MovementCategory movementCategory
) {
}