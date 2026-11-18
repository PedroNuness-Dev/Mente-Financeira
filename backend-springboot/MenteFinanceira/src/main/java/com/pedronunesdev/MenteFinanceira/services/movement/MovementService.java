package com.pedronunesdev.MenteFinanceira.services.movement;

import com.pedronunesdev.MenteFinanceira.domain.movement.Movement;
import com.pedronunesdev.MenteFinanceira.domain.wallet.Wallet;
import com.pedronunesdev.MenteFinanceira.dto.movement.AnalysisMovementCategory;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementResponse;
import com.pedronunesdev.MenteFinanceira.dto.movement.PercentageMovementCategory;
import com.pedronunesdev.MenteFinanceira.dto.movement.TotalByCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;
import com.pedronunesdev.MenteFinanceira.mappers.movement.MovementMapper;
import com.pedronunesdev.MenteFinanceira.repositories.movement.MovementRepository;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import com.pedronunesdev.MenteFinanceira.services.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementService {

    private final MovementRepository movementRepository;
    private final AuthenticationService authenticationService;
    private final MovementMapper movementMapper;
    private final WalletService walletService;

    // Method consumed by WalletService, since a movement is only registered together with some action on the Wallet
    @Transactional
    public MovementResponse registerMovement(
            BigDecimal movedAmount,
            String description,
            MovementType movementType,
            String movementCategory,
            Wallet wallet
    ){
        log.info("Registrando movimentação. tipo={}, categoria={}, walletId={}",
                movementType, movementCategory, wallet.getWalletId());
        log.debug("Valor da movimentação a registrar: {}", movedAmount);

        MovementCategory movementCategoryFound = MovementCategory.from(movementCategory);

        Movement movementToSave = Movement.builder()
                .description(description)
                .movedAmount(movedAmount)
                .movementType(movementType)
                .movementCategory(movementCategoryFound)
                .wallet(wallet)
                .build();

        Movement savedMovement = movementRepository.save(movementToSave);

        log.info("Movimentação registrada com sucesso. movementId={}, walletId={}",
                savedMovement.getMovementId(), wallet.getWalletId());

        return new MovementResponse(
                savedMovement.getMovementId(),
                savedMovement.getMovedAmount(),
                savedMovement.getExecutionDate(),
                savedMovement.getMovementType(),
                savedMovement.getMovementCategory()
        );
    }

    @Transactional(readOnly = true)
    public Page<MovementResponse> findMovementHistory(LocalDate initialDate, LocalDate finalDate, Pageable pageable){

        validateDateRequest(initialDate,finalDate);

        Page<Movement> movements;

        Long userId = authenticationService.extractAuthenticatedUserId();

        log.info("Buscando histórico de movimentações. userId={}, inicio={}, fim={}, pagina={}, tamanho={}",
                userId, initialDate, finalDate, pageable.getPageNumber(), pageable.getPageSize());

        if (initialDate == null && finalDate == null){
            movements = movementRepository.findAllMovementsByUser(userId, pageable);

            log.info("Histórico completo retornado. userId={}, registrosNaPagina={}, totalDeRegistros={}",
                    userId, movements.getNumberOfElements(), movements.getTotalElements());

            return movements.map(movementMapper::toDto);
        }
        else{
            return findMovementByDate(userId, initialDate, finalDate, pageable);
        }
    }

    @Transactional(readOnly = true)
    public AnalysisMovementCategory analyzeMovementsByMonth(LocalDate initialDate, LocalDate finalDate){

        validatePeriodDate(initialDate,finalDate);

        LocalDateTime initialDateFormatWithTime = formatInitialDate(initialDate);
        LocalDateTime finalDateFormatWithTime = formatFinalDate(finalDate);

        Long userId = authenticationService.extractAuthenticatedUserId();

        log.info("Gerando análise de movimentações. userId={}, inicio={}, fim={}",
                userId, initialDate, finalDate);

        List<TotalByCategory> categoryTotals =
                movementRepository.totalByCategory(userId, initialDateFormatWithTime, finalDateFormatWithTime);

        log.info("Categorias encontradas na consulta: {}", categoryTotals.size());

        if (categoryTotals.isEmpty()) {
            log.info("Nenhuma movimentação encontrada no período. userId={}, inicio={}, fimExclusivo={}",
                    userId, initialDateFormatWithTime, finalDateFormatWithTime);
        }

        BigDecimal grandTotal = sumTotals(categoryTotals, null);
        BigDecimal totalWithdrawal = sumTotals(categoryTotals, MovementType.RETIRADA);
        BigDecimal totalIncome = sumTotals(categoryTotals, MovementType.ENTRADA);

        log.debug("Totais calculados. userId={}, geral={}, entradas={}, retiradas={}",
                userId, grandTotal, totalIncome, totalWithdrawal);

        BigDecimal userBalance = walletService.getUserBalance().balance();

        List<PercentageMovementCategory> categoryPercentages = categoryTotals.stream()
                .map(total -> {

                    BigDecimal totalForType = total.movementType() == MovementType.RETIRADA
                            ? totalWithdrawal
                            : totalIncome;

                    BigDecimal percentage;

                    if (totalForType.compareTo(BigDecimal.ZERO) == 0) {
                        log.warn("Total do tipo {} é zero; percentual da categoria {} definido como 0. userId={}",
                                total.movementType(), total.category(), userId);
                        percentage = BigDecimal.ZERO;
                    } else {
                        percentage = total.totalAmount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalForType, 2, RoundingMode.HALF_UP);
                    }

                    return new PercentageMovementCategory(
                            total.category(),
                            total.movementType(),
                            total.totalAmount(),
                            percentage
                    );
                })
                .toList();


        return new AnalysisMovementCategory(
                grandTotal,
                totalIncome,
                totalWithdrawal,
                userBalance,
                categoryPercentages
        );
    }

    private Page<MovementResponse> findMovementByDate(Long userId, LocalDate initialDate, LocalDate finalDate, Pageable pageable){

        validatePeriodDate(initialDate,finalDate);

        LocalDateTime initialDateFormatWithTime = formatInitialDate(initialDate);
        LocalDateTime finalDateFormatWithTime = formatFinalDate(finalDate);

        Page<Movement> movements = movementRepository.findMovementsByDate(userId, initialDateFormatWithTime, finalDateFormatWithTime, pageable);

        log.info("Histórico por período retornado. userId={}, inicio={}, fim={}, registrosNaPagina={}, totalDeRegistros={}",
                userId, initialDate, finalDate, movements.getNumberOfElements(), movements.getTotalElements());

        return movements.map(movementMapper::toDto);
    }

    private LocalDateTime formatInitialDate(LocalDate initialDate){
        return initialDate.atStartOfDay();
    }

    private LocalDateTime formatFinalDate(LocalDate finalDate){
        return finalDate.plusDays(1).atStartOfDay();
    }

    private BigDecimal sumTotals(List<TotalByCategory> categoryTotals, MovementType movementType){
        if (movementType != null){
            return categoryTotals
                    .stream()
                    .filter(movement -> movement.movementType() == movementType)
                    .map(TotalByCategory::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        else {
            return categoryTotals
                    .stream()
                    .map(TotalByCategory::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private void validateDateRequest(LocalDate initialDate, LocalDate finalDate){
        if ((initialDate == null && finalDate != null) || (initialDate != null && finalDate == null)){
            log.warn("Requisição com apenas uma das datas informada. inicio={}, fim={}", initialDate, finalDate);
            throw new IllegalArgumentException("As datas passadas para busca estão em desconformidade, as duas são obrigatórias para a busca");
        }
    }

    private void validatePeriodDate(LocalDate initialDate, LocalDate finalDate){
        if (initialDate.isAfter(finalDate)){
            log.warn("Data inicial posterior à final. inicio={}, fim={}",
                    initialDate, finalDate);
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final para busca");
        }
    }
}