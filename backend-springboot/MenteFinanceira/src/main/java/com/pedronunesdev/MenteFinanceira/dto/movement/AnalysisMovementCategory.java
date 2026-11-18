package com.pedronunesdev.MenteFinanceira.dto.movement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AnalysisMovementCategory(
        BigDecimal totalAmount,
        BigDecimal income,
        BigDecimal withdrawal,
        BigDecimal currentBalance,
        List<PercentageMovementCategory> categoryBreakdown
) {
}