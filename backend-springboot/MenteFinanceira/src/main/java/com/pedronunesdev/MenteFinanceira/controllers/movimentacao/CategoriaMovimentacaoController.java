package com.pedronunesdev.MenteFinanceira.controllers.movimentacao;

import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;
import com.pedronunesdev.MenteFinanceira.services.movimentacao.CategoriaMovimentacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/movimentacao/categoria")
@RequiredArgsConstructor
@Tag(name = "Categoria Movimentacao Controller", description = "Responsável pelas ações relacioandas as categorias das movimentações")
public class CategoriaMovimentacaoController {

    private final CategoriaMovimentacaoService categoriaMovimentacaoService;

    @Operation(summary = "Busca todas as movimentações",
            description = "Realiza a busca de todas as movimentações registradas no sistema")
    @GetMapping
    public ResponseEntity<List<CategoriaMovimentacao>> buscarTodasAsCategoriasDemovimentacao(){

        List<CategoriaMovimentacao> responses = categoriaMovimentacaoService.buscarTodasAsCategoriasDeMovimentacao();

        return ResponseEntity.ok(responses);
    }
}
