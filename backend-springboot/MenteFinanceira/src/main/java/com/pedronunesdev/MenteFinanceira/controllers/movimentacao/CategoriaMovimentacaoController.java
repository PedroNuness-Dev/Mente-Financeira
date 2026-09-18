package com.pedronunesdev.MenteFinanceira.controllers.movimentacao;

import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;
import com.pedronunesdev.MenteFinanceira.services.movimentacao.CategoriaMovimentacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/movimentacao/categoria")
@RequiredArgsConstructor
public class CategoriaMovimentacaoController {

    private final CategoriaMovimentacaoService categoriaMovimentacaoService;

    @GetMapping
    public ResponseEntity<List<CategoriaMovimentacao>> buscarTodasAsCategoriasDemovimentacao(){

        List<CategoriaMovimentacao> responses = categoriaMovimentacaoService.buscarTodasAsCategoriasDeMovimentacao();

        return ResponseEntity.ok(responses);
    }
}
