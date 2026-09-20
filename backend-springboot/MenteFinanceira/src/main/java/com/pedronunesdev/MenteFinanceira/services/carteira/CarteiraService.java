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

        if (carteiraRepository.buscarCarteiraPeloIdDoUsuario(usuario.getId()).isPresent()){
            throw new CarteiraDoUsuarioJaExistenteException("O usuário pode ter apenas uma carteira");
        }

        Carteira carteiraParaSalvar = Carteira.builder()
                .saldo(request.saldoInicial())
                .usuario(usuario)
                .build();

        carteiraRepository.save(carteiraParaSalvar);

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(
                        carteiraParaSalvar.getSaldo(),
                        TipoMovimentacao.ENTRADA,
                        "DEPOSITO" ,
                        carteiraParaSalvar
                );

        return new CarteiraDTOResponse(
                carteiraParaSalvar.getId(),
                carteiraParaSalvar.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    @Transactional
    public CarteiraDTOResponse depositarCarteira(DepositoCarteiraRequest request){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        Carteira carteira = carteiraRepository.buscarCarteiraPeloIdDoUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("O usuário não possui uma carteira!"));

        carteira.depositar(request.valorDeposito());
        carteiraRepository.save(carteira);

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(
                        request.valorDeposito(),
                        TipoMovimentacao.ENTRADA,
                        request.categoriaMovimentacao(),
                        carteira
                );

        return new CarteiraDTOResponse(
                carteira.getId(),
                carteira.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    @Transactional
    public CarteiraDTOResponse saquarCarteira(SaqueCarteiraRequest request){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        Carteira carteira = carteiraRepository.buscarCarteiraPeloIdDoUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("O usuário não possui uma carteira!"));

        carteira.saquar(request.valorSaque());
        carteiraRepository.save(carteira);

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(
                        request.valorSaque(),
                        TipoMovimentacao.RETIRADA,
                        request.categoriaMovimentacao(),
                        carteira
                );

        return new CarteiraDTOResponse(
                carteira.getId(),
                carteira.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    public SaldoDTOResponse consultarSaldo(){

        Long idUsuario = authenticationService.extrairIdDoUsuarioAutenticado();

        BigDecimal saldoBuscado = carteiraRepository.constultarSaldo(idUsuario);

        return new SaldoDTOResponse(saldoBuscado);
    }
}
