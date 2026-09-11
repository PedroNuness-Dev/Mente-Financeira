package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

import java.math.BigDecimal;

public record CategoriaMovimentacaoPercentualDTOResponse(
        CategoriaMovimentacao categoria,
        BigDecimal totalMovimentado,
        BigDecimal percentual
) {}