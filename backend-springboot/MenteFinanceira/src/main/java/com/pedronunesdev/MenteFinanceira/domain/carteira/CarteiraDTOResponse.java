package com.pedronunesdev.MenteFinanceira.domain.carteira;

import com.pedronunesdev.MenteFinanceira.domain.movimentacao.MovimentacaoDTOResponse;

import java.math.BigDecimal;

public record CarteiraDTOResponse(
        Long id,
        BigDecimal saldo,
        MovimentacaoDTOResponse movimentacaoDTOResponse
) {
}
