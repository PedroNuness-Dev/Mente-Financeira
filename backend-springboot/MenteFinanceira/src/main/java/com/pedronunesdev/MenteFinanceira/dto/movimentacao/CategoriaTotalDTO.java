package com.pedronunesdev.MenteFinanceira.dto.movimentacao;

import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;

import java.math.BigDecimal;

public record CategoriaTotalDTO(
        CategoriaMovimentacao categoria,
        BigDecimal totalMovimentado
) {
}
