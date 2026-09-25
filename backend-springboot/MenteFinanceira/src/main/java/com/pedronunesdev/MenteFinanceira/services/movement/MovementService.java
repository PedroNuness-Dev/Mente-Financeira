package com.pedronunesdev.MenteFinanceira.services.movement;

import com.pedronunesdev.MenteFinanceira.domain.movement.Movement;
import com.pedronunesdev.MenteFinanceira.domain.wallet.Wallet;
import com.pedronunesdev.MenteFinanceira.dto.movement.CategoryTotalDTO;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementCategoryAnalysisDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementCategoryPercentageDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementDTOResponse;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;
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
public class MovementService {

    private final MovementRepository movementRepository;
    private final AuthenticationService authenticationService;
    private final WalletRepository walletRepository;

    // Method consumed by WalletService, since a movement is only registered together with some action on the Wallet
    public MovementDTOResponse registerMovement(
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
                savedMovement.getId(), wallet.getWalletId());

        return new MovementDTOResponse(
                savedMovement.getId(),
                savedMovement.getMovedAmount(),
                savedMovement.getExecutionDate(),
                savedMovement.getMovementType(),
                savedMovement.getMovementCategory()
        );
    }

    public Page<MovementDTOResponse> findMovementHistory(Pageable pageable){

        Long userId = authenticationService.extractAuthenticatedUserId();

        log.info("Fetching movement history. userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<MovementDTOResponse> page = movementRepository.movementHistory(userId, pageable);

        log.info("History returned. userId={}, recordsOnPage={}, totalRecords={}",
                userId, page.getNumberOfElements(), page.getTotalElements());

        return page;
    }

    public MovementCategoryAnalysisDTOResponse analyzeMovementsByMonth(Integer month, Integer year){

        log.info("Starting movement analysis. month={}, year={}", month, year);

        validatePeriod(month, year);

        LocalDateTime firstDay = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        String currentMonthName = firstDay.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));

        LocalDateTime analysisDate = LocalDateTime.now();

        Long userId = authenticationService.extractAuthenticatedUserId();

        List<CategoryTotalDTO> categoryTotals =
                movementRepository.totalByCategory(userId, firstDay, lastDay);

        log.info("Categories found in query: {}", categoryTotals.size());

        if (categoryTotals.isEmpty()) {
            log.info("No movements found. userId={}, period={}/{}", userId, month, year);
        }

        BigDecimal grandTotal = sumTotals(categoryTotals, null);
        BigDecimal totalWithdrawal = sumTotals(categoryTotals, MovementType.RETIRADA);
        BigDecimal totalIncome = sumTotals(categoryTotals, MovementType.ENTRADA);

        log.debug("Totals calculated. userId={}, overall={}, income={}, withdrawal={}",
                userId, grandTotal, totalIncome, totalWithdrawal);

        BigDecimal currentBalance = walletRepository.checkBalance(userId);

        List<MovementCategoryPercentageDTOResponse> categoryPercentages = categoryTotals.stream()
                .map(totalDTO -> {

                    BigDecimal totalForType = totalDTO.movementType() == MovementType.RETIRADA
                            ? totalWithdrawal
                            : totalIncome;

                    BigDecimal percentage;
                    if (totalForType.compareTo(BigDecimal.ZERO) == 0) {
                        log.warn("Total for type {} is zero; percentage for category {} set to 0. userId={}",
                                totalDTO.movementType(), totalDTO.category(), userId);
                        percentage = BigDecimal.ZERO;
                    } else {
                        percentage = totalDTO.totalAmount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalForType, 2, RoundingMode.HALF_UP);
                    }

                    return new MovementCategoryPercentageDTOResponse(
                            totalDTO.category(),
                            totalDTO.movementType(),
                            totalDTO.totalAmount(),
                            percentage
                    );
                })
                .toList();

        log.info("Analysis completed. userId={}, period={}/{}, categories={}",
                userId, month, year, categoryPercentages.size());

        return new MovementCategoryAnalysisDTOResponse(
                grandTotal,
                totalIncome,
                totalWithdrawal,
                currentBalance,
                currentMonthName,
                year,
                analysisDate,
                categoryPercentages
        );
    }

    private BigDecimal sumTotals(List<CategoryTotalDTO> categoryTotals, MovementType movementType){
        if (movementType != null){
            return categoryTotals
                    .stream()
                    .filter(movement -> movement.movementType() == movementType)
                    .map(CategoryTotalDTO::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        else {
            return categoryTotals
                    .stream()
                    .map(CategoryTotalDTO::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private void validatePeriod(Integer month, Integer year){

        validateMonth(month);
        validateYear(year);

        YearMonth requestedPeriod = YearMonth.of(year, month);
        if (requestedPeriod.isAfter(YearMonth.now())) {
            log.warn("Requested period is in the future: {}/{}", month, year);
            throw new IllegalArgumentException("The requested period is in the future.");
        }

        log.debug("Period validated: {}/{}", month, year);
    }

    private void validateMonth(Integer month){
        if (month == null || month < 1 || month > 12) {
            log.warn("Invalid month received: {}", month);
            throw new IllegalArgumentException("Invalid month. The value must be between 1 and 12.");
        }
    }

    private void validateYear(Integer year){

        int userCreationYear = authenticationService.extractAuthenticatedUserCreationYear();

        if (year == null){
            log.warn("Null year received in movement search");
            throw new IllegalArgumentException("The year for the search cannot be null");
        }
        if (year < userCreationYear || year > Year.now().getValue()){
            log.warn("Year outside allowed range. year={}, userCreationYear={}, currentYear={}",
                    year, userCreationYear, Year.now().getValue());
            throw new IllegalArgumentException("Invalid year for search: the year cannot be in the future or before the user's creation date.");
        }
    }
}