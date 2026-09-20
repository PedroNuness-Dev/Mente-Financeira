package com.pedronunesdev.MenteFinanceira.services.movimentacao;

import com.pedronunesdev.MenteFinanceira.domain.carteira.Carteira;
import com.pedronunesdev.MenteFinanceira.domain.movimentacao.Movimentacao;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.AnaliseMovimentacaoCategoriaDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.CategoriaMovimentacaoPercentualDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.CategoriaTotalDTO;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.MovimentacaoDTOResponse;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.TipoMovimentacao;
import com.pedronunesdev.MenteFinanceira.repositories.movimentacao.MovimentacaoRepository;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final AuthenticationService authenticationService;

    // Metodo consumido pelo CarteiraService, pois uma movimentacao so é registrada com algum movimento na Carteira
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

    public Page<MovimentacaoDTOResponse> buscarHistoricoMovimentacao(Pageable pageable){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        Page<MovimentacaoDTOResponse> page = movimentacaoRepository.historicoMovimentacoes(idUsuario,pageable);

        return page;
    }

    public AnaliseMovimentacaoCategoriaDTOResponse buscarPorcentagensPorCategoriaMovimentacao(Integer mes, Integer ano){

        verificarPeriodo(mes,ano);

        LocalDateTime diaPrimeiro = LocalDateTime.of(ano, mes, 1,0,0);
        LocalDateTime diaUltimo = diaPrimeiro.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        // Lista das categorias de movimentações e seus respectivos valores movimentados
        List<CategoriaTotalDTO> totaisMovimentacoesMovimentado = movimentacaoRepository.totalPorCategoria(idUsuario,diaPrimeiro,diaUltimo);

        // Pega o total de valores movimentados de todas as categorias, para realizar o cálculo de porcentagem
        BigDecimal totalGeral = totaisMovimentacoesMovimentado
                .stream()
                .map(CategoriaTotalDTO::totalMovimentado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoriaMovimentacaoPercentualDTOResponse> movimentacaoPercentualDTOResponses = totaisMovimentacoesMovimentado.stream()
                .map(totalDTO -> {
                    BigDecimal porcentagem = totalGeral.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : totalDTO.totalMovimentado()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalGeral, 2, RoundingMode.HALF_UP);
                    return new CategoriaMovimentacaoPercentualDTOResponse(
                            totalDTO.categoria(),
                            totalDTO.totalMovimentado(),
                            porcentagem
                    );
                })
                .toList();

        String nomeMesAtual = diaPrimeiro.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.of("pt","BR"));

        return new AnaliseMovimentacaoCategoriaDTOResponse(totalGeral, nomeMesAtual, ano ,movimentacaoPercentualDTOResponses);
    }

    private void verificarPeriodo(Integer mes, Integer ano){

        verificarMes(mes);
        verificarAno(ano);

        YearMonth periodoSolicitado = YearMonth.of(ano, mes);
        if (periodoSolicitado.isAfter(YearMonth.now())) {
            throw new IllegalArgumentException("O período solicitado está no futuro.");
        }
    }

    private Integer verificarMes(Integer mes){

        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mês inválido. O valor deve estar entre 1 e 12.");
        }
        return mes;
    }

    private void verificarAno(Integer ano){

        int anoCriacaoUsuario = authenticationService.extrairAnoCriacaoUsuarioAutenticado(); // Pega o ano de criação do usuário para usar como validação na busca

        if (ano == null){
            throw new IllegalArgumentException("O ano para busca não pode ser nulo");
        }
        if (ano < anoCriacaoUsuario ||ano > Year.now().getValue()){
            throw new IllegalArgumentException("Ano para busca inválido, o ano não pode ser no futuro nem antes da data de criação do usuário.");
        }
    }
}
