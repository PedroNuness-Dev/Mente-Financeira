package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

import java.math.BigDecimal;

public record CategoriaTotalDTO(
        CategoriaMovimentacao categoria,
        BigDecimal totalMovimentado
) {
}
