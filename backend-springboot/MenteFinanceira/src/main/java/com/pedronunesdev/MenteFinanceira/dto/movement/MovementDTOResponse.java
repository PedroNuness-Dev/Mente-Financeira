package com.pedronunesdev.MenteFinanceira.dto.movement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovementDTOResponse(
        Long id,
        BigDecimal movedAmount,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime executionDate,
        MovementType movementType,
        MovementCategory movementCategory
) {
}