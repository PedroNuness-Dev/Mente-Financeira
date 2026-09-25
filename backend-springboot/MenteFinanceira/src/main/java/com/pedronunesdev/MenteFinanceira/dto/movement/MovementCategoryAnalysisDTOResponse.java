package com.pedronunesdev.MenteFinanceira.dto.movement;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MovementCategoryAnalysisDTOResponse(
        BigDecimal totalAmount,
        BigDecimal income,
        BigDecimal withdrawal,
        BigDecimal currentBalance,
        String analyzedMonth,
        Integer analyzedYear,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime analysisDate,
        List<MovementCategoryPercentageDTOResponse> categoryBreakdown
) {
}