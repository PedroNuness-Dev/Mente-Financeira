package com.pedronunesdev.MenteFinanceira.controllers.carteira;

import com.pedronunesdev.MenteFinanceira.dto.carteira.*;
import com.pedronunesdev.MenteFinanceira.services.carteira.CarteiraService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario/carteira")
@Tag(name = "Carteira Controller", description = "Responsável por todas as ações relacionadas diretamente a carteira do usuário")
@RequiredArgsConstructor
public class CarteiraController {

    private final CarteiraService carteiraService;

    @PostMapping
    public ResponseEntity<CarteiraDTOResponse> cadastrarCarteira(@RequestBody @Valid CarteiraDTORequest request){

        CarteiraDTOResponse response = carteiraService.cadastrarCarteiraAoUsuario(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/deposito")
    public ResponseEntity<CarteiraDTOResponse> depositarCarteira(@RequestBody @Valid DepositoCarteiraRequest request){

        CarteiraDTOResponse response = carteiraService.depositarCarteira(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/saque")
    public ResponseEntity<CarteiraDTOResponse> saquarCarteira(@RequestBody @Valid SaqueCarteiraRequest request){

        CarteiraDTOResponse response = carteiraService.saquarCarteira(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/saldo")
    public ResponseEntity<SaldoDTOResponse> consultarSaldo(){

        SaldoDTOResponse response = carteiraService.consultarSaldo();

        return ResponseEntity.ok(response);
    }
}
