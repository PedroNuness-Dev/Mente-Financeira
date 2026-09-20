package com.pedronunesdev.MenteFinanceira.dto.movimentacao;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AnaliseMovimentacaoCategoriaDTOResponse(
        BigDecimal totalMovimentado,
        BigDecimal entrada,
        BigDecimal retirada,
        BigDecimal saldoAtual,
        String mesAnalisado,
        Integer anoAnalisado,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime dataDaAnalise,
        List<CategoriaMovimentacaoPercentualDTOResponse> movimentacoes
) {
}
