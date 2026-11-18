package com.pedronunesdev.MenteFinanceira.services.movement;

import com.pedronunesdev.MenteFinanceira.domain.movement.Movement;
import com.pedronunesdev.MenteFinanceira.domain.wallet.Wallet;
import com.pedronunesdev.MenteFinanceira.dto.movement.AnalysisMovementCategory;
import com.pedronunesdev.MenteFinanceira.dto.movement.TotalByCategory;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementResponse;
import com.pedronunesdev.MenteFinanceira.dto.movement.PercentageMovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;
import com.pedronunesdev.MenteFinanceira.mappers.movement.MovementMapper;
import com.pedronunesdev.MenteFinanceira.repositories.movement.MovementRepository;
import com.pedronunesdev.MenteFinanceira.repositories.wallet.WalletRepository;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementService {

    private final MovementRepository movementRepository;
    private final AuthenticationService authenticationService;
    private final WalletRepository walletRepository;
    private final MovementMapper movementMapper;

    private static final ZoneId ZONE_ID = ZoneId.of("America/Recife");

    // Method consumed by WalletService, since a movement is only registered together with some action on the Wallet
    public MovementResponse registerMovement(
            BigDecimal movedAmount,
            String description,
            MovementType movementType,
            String movementCategory,
            Wallet wallet
    ){
        log.info("Registering movement. type={}, category={}, walletId={}",
                movementType, movementCategory, wallet.getWalletId());
        log.debug("Amount of the movement to register: {}", movedAmount);

        MovementCategory movementCategoryFound = MovementCategory.from(movementCategory);

        Movement movementToSave = Movement.builder()
                .description(description)
                .movedAmount(movedAmount)
                .movementType(movementType)
                .movementCategory(movementCategoryFound)
                .wallet(wallet)
                .build();

        Movement savedMovement = movementRepository.save(movementToSave);

        log.info("Movement registered successfully. movementId={}, walletId={}",
                savedMovement.getMovementId(), wallet.getWalletId());

        return new MovementResponse(
                savedMovement.getMovementId(),
                savedMovement.getMovedAmount(),
                savedMovement.getExecutionDate(),
                savedMovement.getMovementType(),
                savedMovement.getMovementCategory()
        );
    }

    public Page<MovementResponse> findMovementHistory(LocalDate initialDate, LocalDate finalDate, Pageable pageable){

        LocalDateTime initialDateFormatWithTime = formatInitialDate(initialDate);
        LocalDateTime finalDateFormatWithTime = formatFinalDate(finalDate);

        if (initialDateFormatWithTime.isAfter(finalDateFormatWithTime)){
            throw new IllegalArgumentException("Data inicial não pode ser posterior a data final para busca");
        }

        Long userId = authenticationService.extractAuthenticatedUserId();

        log.info("Fetching movement history. userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Movement> movements = movementRepository.movementHistory(userId, initialDateFormatWithTime, finalDateFormatWithTime, pageable);

        log.info("History returned. userId={}, recordsOnPage={}, totalRecords={}",
                userId, movements.getNumberOfElements(), movements.getTotalElements());

        return movements.map(movementMapper::toDto);
    }

    public AnalysisMovementCategory analyzeMovementsByMonth(LocalDate initialDate, LocalDate finalDate){

        LocalDateTime initialDateFormatWithTime = formatInitialDate(initialDate);
        LocalDateTime finalDateFormatWithTime = formatFinalDate(finalDate);

        if (initialDateFormatWithTime.isAfter(finalDateFormatWithTime)){
            throw new IllegalArgumentException("Data inicial não pode ser posterior a data final para busca");
        }

        Long userId = authenticationService.extractAuthenticatedUserId();

        List<TotalByCategory> categoryTotals =
                movementRepository.totalByCategory(userId, initialDateFormatWithTime, finalDateFormatWithTime);

        log.info("Categories found in query: {}", categoryTotals.size());

        if (categoryTotals.isEmpty()) {
            log.info("No movements found. userId={}, period={}/{}", userId, initialDateFormatWithTime, finalDateFormatWithTime);
        }

        BigDecimal grandTotal = sumTotals(categoryTotals, null);
        BigDecimal totalWithdrawal = sumTotals(categoryTotals, MovementType.RETIRADA);
        BigDecimal totalIncome = sumTotals(categoryTotals, MovementType.ENTRADA);

        log.debug("Totals calculated. userId={}, overall={}, income={}, withdrawal={}",
                userId, grandTotal, totalIncome, totalWithdrawal);

        BigDecimal currentBalance = walletRepository.checkBalance(userId);

        List<PercentageMovementCategory> categoryPercentages = categoryTotals.stream()
                .map(total -> {

                    BigDecimal totalForType = total.movementType() == MovementType.RETIRADA
                            ? totalWithdrawal
                            : totalIncome;

                    BigDecimal percentage;

                    if (totalForType.compareTo(BigDecimal.ZERO) == 0) {
                        log.warn("Total for type {} is zero; percentage for category {} set to 0. userId={}",
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
                currentBalance,
                categoryPercentages
        );
    }

    private LocalDateTime formatInitialDate(LocalDate initialDate){
        LocalDate initialDateFormat = initialDate != null ? initialDate : getLocalDateNowWithZoneId();
        return initialDateFormat.atStartOfDay();
    }

    private LocalDateTime formatFinalDate(LocalDate finalDate){
        LocalDate finalDateFormat = finalDate != null ? finalDate : getLocalDateNowWithZoneId();
        return finalDateFormat.plusDays(1).atStartOfDay();
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

    private LocalDate getLocalDateNowWithZoneId(){
        return LocalDate.now(ZONE_ID);
    }
}