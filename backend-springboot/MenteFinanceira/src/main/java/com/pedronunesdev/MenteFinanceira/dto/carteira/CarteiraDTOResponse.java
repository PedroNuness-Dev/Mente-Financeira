package com.pedronunesdev.MenteFinanceira.dto.carteira;

import com.pedronunesdev.MenteFinanceira.dto.movimentacao.MovimentacaoDTOResponse;

import java.math.BigDecimal;

public record CarteiraDTOResponse(
        Long id,
        BigDecimal saldo,
        MovimentacaoDTOResponse movimentacaoDTOResponse
) {
}
