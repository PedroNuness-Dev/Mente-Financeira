package com.pedronunesdev.MenteFinanceira.services.carteira;

import com.pedronunesdev.MenteFinanceira.domain.carteira.Carteira;
import com.pedronunesdev.MenteFinanceira.domain.usuario.Usuario;
import com.pedronunesdev.MenteFinanceira.dto.carteira.*;
import com.pedronunesdev.MenteFinanceira.dto.movimentacao.MovimentacaoDTOResponse;
import com.pedronunesdev.MenteFinanceira.enums.movimentacao.TipoMovimentacao;
import com.pedronunesdev.MenteFinanceira.exception.CarteiraDoUsuarioJaExistenteException;
import com.pedronunesdev.MenteFinanceira.exception.ResourceNotFoundException;
import com.pedronunesdev.MenteFinanceira.repositories.carteira.CarteiraRepository;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import com.pedronunesdev.MenteFinanceira.services.movimentacao.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarteiraService {

    private final CarteiraRepository carteiraRepository;
    private final MovimentacaoService movimentacaoService;
    private final AuthenticationService authenticationService;

    @Transactional
    public CarteiraDTOResponse cadastrarCarteiraAoUsuario(CarteiraDTORequest request){

        Usuario usuario = authenticationService.me();

        log.info("Iniciando cadastro de carteira. usuarioId={}", usuario.getId());

        if (carteiraRepository.buscarCarteiraPeloIdDoUsuario(usuario.getId()).isPresent()){
            log.warn("Tentativa de cadastrar segunda carteira. usuarioId={}", usuario.getId());
            throw new CarteiraDoUsuarioJaExistenteException("O usuário pode ter apenas uma carteira");
        }

        log.info("Saldo inicial da carteira: {}. usuarioId={}", request.saldoInicial(), usuario.getId());

        Carteira carteiraParaSalvar = Carteira.builder()
                .saldo(request.saldoInicial())
                .usuario(usuario)
                .build();

        carteiraRepository.save(carteiraParaSalvar);

        log.debug("Carteira salva. carteiraId={}, usuarioId={}", carteiraParaSalvar.getId(), usuario.getId());

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(
                        carteiraParaSalvar.getSaldo(),
                        TipoMovimentacao.ENTRADA,
                        "DEPOSITO" ,
                        carteiraParaSalvar
                );

        log.info("Carteira cadastrada com sucesso. carteiraId={}, usuarioId={}, movimentacaoId={}",
                carteiraParaSalvar.getId(), usuario.getId(), movimentacaoDTOResponse.id());

        return new CarteiraDTOResponse(
                carteiraParaSalvar.getId(),
                carteiraParaSalvar.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    @Transactional
    public CarteiraDTOResponse depositarCarteira(DepositoCarteiraRequest request){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        log.info("Iniciando depósito. usuarioId={}, categoria={}", idUsuario, request.categoriaMovimentacao());
        log.info("Valor do depósito: {}. usuarioId={}", request.valorDeposito(), idUsuario);

        Carteira carteira = carteiraRepository.buscarCarteiraPeloIdDoUsuario(idUsuario)
                .orElseThrow(() -> {
                    log.warn("Depósito recusado: usuário sem carteira. usuarioId={}", idUsuario);
                    return new ResourceNotFoundException("O usuário não possui uma carteira!");
                });

        carteira.depositar(request.valorDeposito());
        carteiraRepository.save(carteira);

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(
                        request.valorDeposito(),
                        TipoMovimentacao.ENTRADA,
                        request.categoriaMovimentacao(),
                        carteira
                );

        log.info("Depósito realizado com sucesso. carteiraId={}, usuarioId={}, movimentacaoId={}",
                carteira.getId(), idUsuario, movimentacaoDTOResponse.id());

        return new CarteiraDTOResponse(
                carteira.getId(),
                carteira.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    @Transactional
    public CarteiraDTOResponse saquarCarteira(SaqueCarteiraRequest request){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        log.info("Iniciando saque. usuarioId={}, categoria={}", idUsuario, request.categoriaMovimentacao());
        log.info("Valor do saque: {}. usuarioId={}", request.valorSaque(), idUsuario);

        Carteira carteira = carteiraRepository.buscarCarteiraPeloIdDoUsuario(idUsuario)
                .orElseThrow(() -> {
                    log.warn("Saque recusado: usuário sem carteira. usuarioId={}", idUsuario);
                    return new ResourceNotFoundException("O usuário não possui uma carteira!");
                });

        carteira.saquar(request.valorSaque());
        carteiraRepository.save(carteira);

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(
                        request.valorSaque(),
                        TipoMovimentacao.RETIRADA,
                        request.categoriaMovimentacao(),
                        carteira
                );

        log.info("Saque realizado com sucesso. carteiraId={}, usuarioId={}, movimentacaoId={}",
                carteira.getId(), idUsuario, movimentacaoDTOResponse.id());

        return new CarteiraDTOResponse(
                carteira.getId(),
                carteira.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    public SaldoDTOResponse consultarSaldo(){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        log.info("Consultando saldo. usuarioId={}", idUsuario);

        BigDecimal saldoBuscado = carteiraRepository.consultarSaldo(idUsuario);

        log.info("Saldo consultado: {}. usuarioId={}", saldoBuscado, idUsuario);

        return new SaldoDTOResponse(saldoBuscado);
    }
}