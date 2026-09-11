package com.pedronunesdev.MenteFinanceira.domain.carteira;

import com.pedronunesdev.MenteFinanceira.domain.movimentacao.MovimentacaoDTOResponse;
import com.pedronunesdev.MenteFinanceira.domain.movimentacao.MovimentacaoService;
import com.pedronunesdev.MenteFinanceira.domain.movimentacao.TipoMovimentacao;
import com.pedronunesdev.MenteFinanceira.domain.usuario.Usuario;
import com.pedronunesdev.MenteFinanceira.domain.usuario.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoService movimentacaoService;

    @Transactional
    public CarteiraDTOResponse cadastrarCarteiraAoUsuario(CarteiraDTORequest request, Long idUsuario){

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Carteira carteiraParaSalvar = Carteira.builder()
                .saldo(request.saldoInicial())
                .usuario(usuario)
                .build();

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(carteiraParaSalvar.getSaldo(), TipoMovimentacao.ENTRADA,"DEPOSITO" ,carteiraParaSalvar);

        carteiraRepository.save(carteiraParaSalvar);

        return new CarteiraDTOResponse(
                carteiraParaSalvar.getId(),
                carteiraParaSalvar.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    @Transactional
    public CarteiraDTOResponse depositarCarteira(DepositoCarteiraRequest request, Long idCarteira){

        Carteira carteira = carteiraRepository.findById(idCarteira)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada"));

        carteira.depositar(request.valorDeposito());
        carteiraRepository.save(carteira);

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(request.valorDeposito(), TipoMovimentacao.ENTRADA, request.categoriaMovimentacao(), carteira);

        return new CarteiraDTOResponse(
                carteira.getId(),
                carteira.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    @Transactional
    public CarteiraDTOResponse saquarCarteira(SaqueCarteiraRequest request, Long idCarteira){

        Carteira carteira = carteiraRepository.findById(idCarteira)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada"));

        carteira.saquar(request.valorSaque());
        carteiraRepository.save(carteira);

        MovimentacaoDTOResponse movimentacaoDTOResponse =
                movimentacaoService.registrarMovimentacao(request.valorSaque(), TipoMovimentacao.RETIRADA, request.categoriaMovimentacao(), carteira);

        return new CarteiraDTOResponse(
                carteira.getId(),
                carteira.getSaldo(),
                movimentacaoDTOResponse
        );
    }

    public SaldoDTOResponse consultarSaldo(Long idUsuario){

        BigDecimal saldoBuscado = carteiraRepository.constultarSaldo(idUsuario);

        return new SaldoDTOResponse(saldoBuscado);
    }
}
