package com.pedronunesdev.MenteFinanceira.services.movimentacao;

import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class CategoriaMovimentacaoService {

    public List<CategoriaMovimentacao> buscarTodasAsCategoriasDeMovimentacao(){

        log.info("Iniciando busca de todas as categorias de movimentacao regsitradas no sistema");

        return Arrays.stream(CategoriaMovimentacao.values())
                .toList();
    }
}
