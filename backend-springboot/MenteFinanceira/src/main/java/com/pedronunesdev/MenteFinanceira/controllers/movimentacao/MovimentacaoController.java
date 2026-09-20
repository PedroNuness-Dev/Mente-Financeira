package com.pedronunesdev.MenteFinanceira.controllers.movimentacao;

import com.pedronunesdev.MenteFinanceira.dto.movimentacao.AnaliseMovimentacaoCategoriaDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.MovimentacaoDTOResponse;
import com.pedronunesdev.MenteFinanceira.services.movimentacao.MovimentacaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuario/carteira/movimentacoes")
@Tag(name = "Movimentacao Controller", description = "Responsável por todas as ações relacionadas as movimentações do usuário")
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @GetMapping
    public ResponseEntity<Page<MovimentacaoDTOResponse>> buscarHistoricoMovimentacoes(Pageable pageable){

        Page<MovimentacaoDTOResponse> page = movimentacaoService.buscarHistoricoMovimentacao(pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{mes}/{ano}/porcentagem")
    public ResponseEntity<AnaliseMovimentacaoCategoriaDTOResponse> buscarPorcentagesPorCategoriaMovimentacao(
            @PathVariable Integer mes, @PathVariable Integer ano){

        AnaliseMovimentacaoCategoriaDTOResponse response = movimentacaoService.buscarPorcentagensPorCategoriaMovimentacao(mes,ano);

        return ResponseEntity.ok(response);
    }
}
