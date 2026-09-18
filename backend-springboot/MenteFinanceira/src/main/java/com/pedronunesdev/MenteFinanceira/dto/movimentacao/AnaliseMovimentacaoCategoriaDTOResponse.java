package com.pedronunesdev.MenteFinanceira.dto.movimentacao;

import java.math.BigDecimal;
import java.util.List;

public record AnaliseMovimentacaoCategoriaDTOResponse(
        BigDecimal totalMovimentado,
        Integer mesAnalisado,
        List<CategoriaMovimentacaoPercentualDTOResponse> movimentacoes
) {
}
