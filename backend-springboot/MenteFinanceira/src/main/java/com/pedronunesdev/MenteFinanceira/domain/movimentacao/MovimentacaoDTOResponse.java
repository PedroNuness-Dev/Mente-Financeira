package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoDTOResponse(
        Long id,
        BigDecimal valorMovimentado,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime dataDeExecucao,
        TipoMovimentacao tipoMovimentacao,
        CategoriaMovimentacao categoriaMovimentacao
) {
}
