package com.pedronunesdev.MenteFinanceira.dto.movimentacao;

import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.TipoMovimentacao;

import java.math.BigDecimal;

public record CategoriaMovimentacaoPercentualDTOResponse(
        CategoriaMovimentacao categoria,
        TipoMovimentacao tipoMovimentacao,
        BigDecimal totalMovimentado,
        BigDecimal percentual
) {}