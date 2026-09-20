package com.pedronunesdev.MenteFinanceira.services.movimentacao;

import com.pedronunesdev.MenteFinanceira.domain.carteira.Carteira;
import com.pedronunesdev.MenteFinanceira.domain.movimentacao.Movimentacao;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.AnaliseMovimentacaoCategoriaDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.CategoriaMovimentacaoPercentualDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.CategoriaTotalDTO;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.MovimentacaoDTOResponse;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.CategoriaMovimentacao;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.TipoMovimentacao;
import com.pedronunesdev.MenteFinanceira.repositories.carteira.CarteiraRepository;
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
    private final CarteiraRepository carteiraRepository;

    // Metodo consumido pelo CarteiraService, pois uma movimentacao so é registrada com algum movimento na Carteira
    public MovimentacaoDTOResponse registrarMovimentacao(
            BigDecimal valorMovimentado,
            TipoMovimentacao tipoMovimentacao,
            String categoriaMovimentacao,
            Carteira carteira
    ){
        log.info("Registrando movimentação. tipo={}, categoria={}, carteiraId={}",
                tipoMovimentacao, categoriaMovimentacao, carteira.getId());
        log.debug("Valor da movimentação a registrar: {}", valorMovimentado);

        CategoriaMovimentacao categoriaMovimentacaoEncontrada = CategoriaMovimentacao.from(categoriaMovimentacao);

        Movimentacao movimentacaoParaSalvar = Movimentacao.builder()
                .valorMovimentado(valorMovimentado)
                .tipoMovimentacao(tipoMovimentacao)
                .categoriaMovimentacao(categoriaMovimentacaoEncontrada)
                .carteira(carteira)
                .build();

        Movimentacao movimentacaoSalva = movimentacaoRepository.save(movimentacaoParaSalvar);

        log.info("Movimentação registrada com sucesso. movimentacaoId={}, carteiraId={}",
                movimentacaoSalva.getId(), carteira.getId());

        return new MovimentacaoDTOResponse(
                movimentacaoSalva.getId(),
                movimentacaoSalva.getValorMovimentado(),
                movimentacaoSalva.getDataDeExecucao(),
                movimentacaoSalva.getTipoMovimentacao(),
                movimentacaoSalva.getCategoriaMovimentacao()
        );
    }

    public Page<MovimentacaoDTOResponse> buscarHistoricoMovimentacao(Pageable pageable){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        log.info("Buscando histórico de movimentações. usuarioId={}, página={}, tamanho={}",
                idUsuario, pageable.getPageNumber(), pageable.getPageSize());

        Page<MovimentacaoDTOResponse> page = movimentacaoRepository.historicoMovimentacoes(idUsuario, pageable);

        log.info("Histórico retornado. usuarioId={}, registrosNaPágina={}, totalDeRegistros={}",
                idUsuario, page.getNumberOfElements(), page.getTotalElements());

        return page;
    }

    public AnaliseMovimentacaoCategoriaDTOResponse analisarMovimentacaosPeloMes(Integer mes, Integer ano){

        log.info("Iniciando análise de movimentações. mes={}, ano={}", mes, ano);

        verificarPeriodo(mes, ano);

        LocalDateTime diaPrimeiro = LocalDateTime.of(ano, mes, 1, 0, 0);
        LocalDateTime diaUltimo = diaPrimeiro.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        String nomeMesAtual = diaPrimeiro.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));

        LocalDateTime dataDaAnalise = LocalDateTime.now();

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        List<CategoriaTotalDTO> totaisMovimentadosPorCategoria =
                movimentacaoRepository.totalPorCategoria(idUsuario, diaPrimeiro, diaUltimo);

        log.info("Categorias encontradas na consulta: {}", totaisMovimentadosPorCategoria.size());

        if (totaisMovimentadosPorCategoria.isEmpty()) {
            log.info("Nenhuma movimentação encontrada. usuarioId={}, periodo={}/{}", idUsuario, mes, ano);
        }

        BigDecimal totalGeral = somarTotais(totaisMovimentadosPorCategoria, null);
        BigDecimal totalRetirada = somarTotais(totaisMovimentadosPorCategoria, TipoMovimentacao.RETIRADA);
        BigDecimal totalEntrada = somarTotais(totaisMovimentadosPorCategoria, TipoMovimentacao.ENTRADA);

        log.debug("Totais calculados. usuarioId={}, geral={}, entrada={}, retirada={}",
                idUsuario, totalGeral, totalEntrada, totalRetirada);

        BigDecimal saldoAtual = carteiraRepository.consultarSaldo(idUsuario);

        List<CategoriaMovimentacaoPercentualDTOResponse> movimentacaoPercentualDTOResponses = totaisMovimentadosPorCategoria.stream()
                .map(totalDTO -> {

                    BigDecimal totalDoTipo = totalDTO.tipoMovimentacao() == TipoMovimentacao.RETIRADA
                            ? totalRetirada
                            : totalEntrada;

                    BigDecimal porcentagem;
                    if (totalDoTipo.compareTo(BigDecimal.ZERO) == 0) {
                        log.warn("Total do tipo {} é zero; porcentagem da categoria {} definida como 0. usuarioId={}",
                                totalDTO.tipoMovimentacao(), totalDTO.categoria(), idUsuario);
                        porcentagem = BigDecimal.ZERO;
                    } else {
                        porcentagem = totalDTO.totalMovimentado()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalDoTipo, 2, RoundingMode.HALF_UP);
                    }

                    return new CategoriaMovimentacaoPercentualDTOResponse(
                            totalDTO.categoria(),
                            totalDTO.tipoMovimentacao(),
                            totalDTO.totalMovimentado(),
                            porcentagem
                    );
                })
                .toList();

        log.info("Análise concluída. usuarioId={}, periodo={}/{}, categorias={}",
                idUsuario, mes, ano, movimentacaoPercentualDTOResponses.size());

        return new AnaliseMovimentacaoCategoriaDTOResponse(
                totalGeral,
                totalEntrada,
                totalRetirada,
                saldoAtual,
                nomeMesAtual,
                ano,
                dataDaAnalise,
                movimentacaoPercentualDTOResponses
        );
    }

    private BigDecimal somarTotais(List<CategoriaTotalDTO> totaisMovimentadosPorCategoria, TipoMovimentacao tipoMovimentacao){
        if (tipoMovimentacao != null){
            return totaisMovimentadosPorCategoria
                    .stream()
                    .filter(movimentacao -> movimentacao.tipoMovimentacao() == tipoMovimentacao)
                    .map(CategoriaTotalDTO::totalMovimentado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        else {
            return totaisMovimentadosPorCategoria
                    .stream()
                    .map(CategoriaTotalDTO::totalMovimentado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private void verificarPeriodo(Integer mes, Integer ano){

        verificarMes(mes);
        verificarAno(ano);

        YearMonth periodoSolicitado = YearMonth.of(ano, mes);
        if (periodoSolicitado.isAfter(YearMonth.now())) {
            log.warn("Período solicitado está no futuro: {}/{}", mes, ano);
            throw new IllegalArgumentException("O período solicitado está no futuro.");
        }

        log.debug("Período validado: {}/{}", mes, ano);
    }

    private void verificarMes(Integer mes){
        if (mes == null || mes < 1 || mes > 12) {
            log.warn("Mês inválido recebido: {}", mes);
            throw new IllegalArgumentException("Mês inválido. O valor deve estar entre 1 e 12.");
        }
    }

    private void verificarAno(Integer ano){

        int anoCriacaoUsuario = authenticationService.extrairAnoCriacaoUsuarioAutenticado();

        if (ano == null){
            log.warn("Ano nulo recebido na busca de movimentações");
            throw new IllegalArgumentException("O ano para busca não pode ser nulo");
        }
        if (ano < anoCriacaoUsuario || ano > Year.now().getValue()){
            log.warn("Ano fora do intervalo permitido. ano={}, anoCriacaoUsuario={}, anoAtual={}",
                    ano, anoCriacaoUsuario, Year.now().getValue());
            throw new IllegalArgumentException("Ano para busca inválido, o ano não pode ser no futuro nem antes da data de criação do usuário.");
        }
    }
}