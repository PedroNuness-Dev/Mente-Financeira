package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuario/carteira/movimentacoes")
@Tag(name = "Movimentacao Controller", description = "Responsável por todas as ações relacionadas as movimentações do usuário")
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @GetMapping
    public ResponseEntity<Page<MovimentacaoDTOResponse>> buscarHistoricoMovimentacoes(@RequestParam Long idUsuario, Pageable pageable){

        Page<MovimentacaoDTOResponse> page = movimentacaoService.buscarHistoricoMovimentacao(idUsuario,pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/porcentagem")
    public ResponseEntity<AnaliseMovimentacaoCategoriaDTOResponse> buscarPorcentagesPorCategoriaMovimentacao(
            @RequestParam Long idUsuario, @RequestParam Integer mes, @RequestParam Integer ano){

        AnaliseMovimentacaoCategoriaDTOResponse response = movimentacaoService.buscarPorcentagensPorCategoriaMovimentacao(idUsuario,mes,ano);

        return ResponseEntity.ok(response);
    }
}
