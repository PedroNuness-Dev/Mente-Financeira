package com.pedronunesdev.MenteFinanceira.controllers.wallet;

import com.pedronunesdev.MenteFinanceira.dto.wallet.*;
import com.pedronunesdev.MenteFinanceira.services.wallet.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/wallet")
@Tag(name = "Wallet Controller", description = "Responsible for all actions directly related to the user's wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> registerWallet(@RequestBody @Valid WalletRegisterRequest request){

        WalletResponse response = walletService.registerWalletForUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/deposit")
    public ResponseEntity<WalletResponse> depositToWallet(@RequestBody @Valid WalletDepositRequest request){

        WalletResponse response = walletService.depositToWallet(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdrawFromWallet(@RequestBody @Valid WalletWithdrawalRequest request){

        WalletResponse response = walletService.withdrawFromWallet(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> checkBalance(){

        BalanceResponse response = walletService.getUserBalance();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<WalletCheckRequest> checkWalletExist(){

        return ResponseEntity.ok(walletService.walletCheckExist());
    }
}