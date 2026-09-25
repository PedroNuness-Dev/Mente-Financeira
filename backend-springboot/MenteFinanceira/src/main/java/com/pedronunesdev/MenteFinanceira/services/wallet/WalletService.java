package com.pedronunesdev.MenteFinanceira.services.wallet;

import com.pedronunesdev.MenteFinanceira.domain.user.User;
import com.pedronunesdev.MenteFinanceira.domain.wallet.Wallet;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.wallet.*;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;
import com.pedronunesdev.MenteFinanceira.exception.ResourceNotFoundException;
import com.pedronunesdev.MenteFinanceira.exception.WalletAlreadyExistsException;
import com.pedronunesdev.MenteFinanceira.repositories.wallet.WalletRepository;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import com.pedronunesdev.MenteFinanceira.services.movement.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final MovementService movementService;
    private final AuthenticationService authenticationService;

    @Transactional
    public WalletResponseDTO registerWalletForUser(WalletRequestDTO request){

        User user = authenticationService.me();

        log.info("Starting wallet registration. userId={}", user.getId());

        if (walletRepository.findWalletByUserId(user.getId()).isPresent()){
            log.warn("Attempt to register a second wallet. userId={}", user.getId());
            throw new WalletAlreadyExistsException("The user can only have one wallet");
        }

        log.info("Initial wallet balance: {}. userId={}", request.initialBalance(), user.getId());

        Wallet walletToSave = Wallet.builder()
                .balance(request.initialBalance())
                .user(user)
                .build();

        walletRepository.save(walletToSave);

        log.debug("Wallet saved. walletId={}, userId={}", walletToSave.getWalletId(), user.getId());

        MovementDTOResponse movementDTOResponse =
                movementService.registerMovement(
                        walletToSave.getBalance(),
                        "Depósito inicial",
                        MovementType.ENTRADA,
                        "DEPOSITO" ,
                        walletToSave
                );

        log.info("Wallet registered successfully. walletId={}, userId={}, movementId={}",
                walletToSave.getWalletId(), user.getId(), movementDTOResponse.id());

        return new WalletResponseDTO(
                walletToSave.getWalletId(),
                walletToSave.getBalance(),
                movementDTOResponse
        );
    }

    @Transactional
    public WalletResponseDTO depositToWallet(WalletDepositRequest request){

        Long userId = authenticationService.extractAuthenticatedUserId();

        log.info("Starting deposit. userId={}, category={}", userId, request.movementCategory());
        log.info("Deposit amount: {}. userId={}", request.depositAmount(), userId);

        Wallet wallet = walletRepository.findWalletByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Deposit refused: user has no wallet. userId={}", userId);
                    return new ResourceNotFoundException("The user does not have a wallet!");
                });

        wallet.deposit(request.depositAmount());
        walletRepository.save(wallet);

        MovementDTOResponse movementDTOResponse =
                movementService.registerMovement(
                        request.depositAmount(),
                        request.description(),
                        MovementType.ENTRADA,
                        request.movementCategory(),
                        wallet
                );

        log.info("Deposit completed successfully. walletId={}, userId={}, movementId={}",
                wallet.getWalletId(), userId, movementDTOResponse.id());

        return new WalletResponseDTO(
                wallet.getWalletId(),
                wallet.getBalance(),
                movementDTOResponse
        );
    }

    @Transactional
    public WalletResponseDTO withdrawFromWallet(WalletWithdrawalRequest request){

        Long userId = authenticationService.extractAuthenticatedUserId();

        log.info("Starting withdrawal. userId={}, category={}", userId, request.movementCategory());
        log.info("Withdrawal amount: {}. userId={}", request.withdrawalAmount(), userId);

        Wallet wallet = walletRepository.findWalletByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Withdrawal refused: user has no wallet. userId={}", userId);
                    return new ResourceNotFoundException("The user does not have a wallet!");
                });

        wallet.withdraw(request.withdrawalAmount());
        walletRepository.save(wallet);

        MovementDTOResponse movementDTOResponse =
                movementService.registerMovement(
                        request.withdrawalAmount(),
                        request.description(),
                        MovementType.RETIRADA,
                        request.movementCategory(),
                        wallet
                );

        log.info("Withdrawal completed successfully. walletId={}, userId={}, movementId={}",
                wallet.getWalletId(), userId, movementDTOResponse.id());

        return new WalletResponseDTO(
                wallet.getWalletId(),
                wallet.getBalance(),
                movementDTOResponse
        );
    }

    public BalanceResponseDTO checkBalance(){

        Long userId = authenticationService.extractAuthenticatedUserId();

        log.info("Checking balance. userId={}", userId);

        BigDecimal foundBalance = walletRepository.checkBalance(userId);

        log.info("Balance checked: {}. userId={}", foundBalance, userId);

        return new BalanceResponseDTO(foundBalance);
    }
}