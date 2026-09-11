package com.pedronunesdev.MenteFinanceira.domain.carteira;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carteira")
@Tag(name = "Carteira Controller", description = "Responsável por todas as ações relacionadas diretamente a carteira do usuário")
@RequiredArgsConstructor
public class CarteiraController {

    private final CarteiraService carteiraService;

    @PostMapping
    public ResponseEntity<CarteiraDTOResponse> cadastrarCarteira(@RequestBody @Valid CarteiraDTORequest request, @RequestParam Long idUsuario){ //TODO: a lógica de buscar o usuário será pelo security context

        CarteiraDTOResponse response = carteiraService.cadastrarCarteiraAoUsuario(request, idUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/deposito")
    public ResponseEntity<CarteiraDTOResponse> depositarCarteira(@RequestBody @Valid DepositoCarteiraRequest request, @RequestParam Long idCarteira){

        CarteiraDTOResponse response = carteiraService.depositarCarteira(request,idCarteira);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/saque")
    public ResponseEntity<CarteiraDTOResponse> saquarCarteira(@RequestBody @Valid SaqueCarteiraRequest request, @RequestParam Long idCarteira){

        CarteiraDTOResponse response = carteiraService.saquarCarteira(request,idCarteira);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/saldo")
    public ResponseEntity<SaldoDTOResponse> consultarSaldo(@RequestParam Long idUsuario){

        SaldoDTOResponse response = carteiraService.consultarSaldo(idUsuario);

        return ResponseEntity.ok(response);
    }
}
