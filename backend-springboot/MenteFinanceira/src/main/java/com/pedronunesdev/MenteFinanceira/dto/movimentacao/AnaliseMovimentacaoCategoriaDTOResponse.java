package com.pedronunesdev.MenteFinanceira.dto.movimentacao;

import java.math.BigDecimal;
import java.util.List;

public record AnaliseMovimentacaoCategoriaDTOResponse(
        BigDecimal totalMovimentado,
        String mesAnalisado,
        Integer anoAnalisado,
        List<CategoriaMovimentacaoPercentualDTOResponse> movimentacoes
) {
}
