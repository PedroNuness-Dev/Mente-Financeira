package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

import java.math.BigDecimal;
import java.util.List;

public record AnaliseMovimentacaoCategoriaDTOResponse(
        BigDecimal totalMovimentado,
        Integer mesAnalisado,
        List<CategoriaMovimentacaoPercentualDTOResponse> movimentacoes
) {
}
