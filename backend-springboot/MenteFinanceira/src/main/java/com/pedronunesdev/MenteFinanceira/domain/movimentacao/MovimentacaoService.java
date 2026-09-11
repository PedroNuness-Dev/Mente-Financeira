package com.pedronunesdev.MenteFinanceira.domain.movimentacao;

import com.pedronunesdev.MenteFinanceira.domain.carteira.Carteira;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;

    public MovimentacaoDTOResponse registrarMovimentacao(
            BigDecimal valorMovimentado,
            TipoMovimentacao tipoMovimentacao,
            String categoriaMovimentacao, // Passando string pois vem junto com a requisição para ser passado para o ENUM
            Carteira carteira
    ){
        CategoriaMovimentacao categoriaMovimentacaoEncontrada = CategoriaMovimentacao.from(categoriaMovimentacao); // Busco o ENUM de categoria

        Movimentacao movimentacaoParaSalvar = Movimentacao.builder()
                .valorMovimentado(valorMovimentado)
                .tipoMovimentacao(tipoMovimentacao)
                .categoriaMovimentacao(categoriaMovimentacaoEncontrada)
                .carteira(carteira)
                .build();

        movimentacaoRepository.save(movimentacaoParaSalvar);

        return new MovimentacaoDTOResponse(
                movimentacaoParaSalvar.getId(),
                movimentacaoParaSalvar.getValorMovimentado(),
                movimentacaoParaSalvar.getDataDeExecucao(),
                movimentacaoParaSalvar.getTipoMovimentacao(),
                movimentacaoParaSalvar.getCategoriaMovimentacao()
        );
    }

    public Page<MovimentacaoDTOResponse> buscarHistoricoMovimentacao(Long idUsuario, Pageable pageable){

        Page<MovimentacaoDTOResponse> page = movimentacaoRepository.historicoMovimentacoes(idUsuario,pageable);

        return page;
    }

    public AnaliseMovimentacaoCategoriaDTOResponse buscarPorcentagensPorCategoriaMovimentacao(Long idUsuario,Integer mes, Integer ano){

        Integer mesParaBuscar = verificarMes(mes);

        LocalDateTime diaPrimeiro = LocalDateTime.of(ano, mesParaBuscar, 1,0,0);
        LocalDateTime diaUltimo = diaPrimeiro.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        // Lista das categorias de movimentações e seus respectivos valores movimentados
        List<CategoriaTotalDTO> totaisMovimentacoesMovimentado = movimentacaoRepository.totalPorCategoria(idUsuario,diaPrimeiro,diaUltimo);

        // Pega o total de valores movimentados de todas as categorias, para realizar o cálculo de porcentagem
        BigDecimal totalGeral = totaisMovimentacoesMovimentado
                .stream()
                .map(CategoriaTotalDTO::totalMovimentado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoriaMovimentacaoPercentualDTOResponse> movimentacaoPercentualDTOResponses = totaisMovimentacoesMovimentado.stream()
                .map(totalDTO -> {
                    BigDecimal porcentagem = (totalDTO.totalMovimentado()
                            .divide(totalGeral, 2, RoundingMode.HALF_UP))
                            .multiply(BigDecimal.valueOf(100));
                    return new CategoriaMovimentacaoPercentualDTOResponse(
                            totalDTO.categoria(),
                            totalDTO.totalMovimentado(),
                            porcentagem
                    );
                })
                .toList();

        return new AnaliseMovimentacaoCategoriaDTOResponse(totalGeral, mesParaBuscar ,movimentacaoPercentualDTOResponses);
    }

    private Integer verificarMes(Integer mes){

        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mês inválido. O valor deve estar entre 1 e 12.");
        }
        return mes;
    }
}
