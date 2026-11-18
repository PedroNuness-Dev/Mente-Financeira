package com.pedronunesdev.MenteFinanceira.services.movement;

import com.pedronunesdev.MenteFinanceira.domain.movement.Movement;
import com.pedronunesdev.MenteFinanceira.domain.wallet.Wallet;
import com.pedronunesdev.MenteFinanceira.dto.movement.AnalysisMovementCategory;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementResponse;
import com.pedronunesdev.MenteFinanceira.dto.movement.PercentageMovementCategory;
import com.pedronunesdev.MenteFinanceira.dto.movement.TotalByCategory;
import com.pedronunesdev.MenteFinanceira.dto.wallet.BalanceResponse;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.enums.movement.MovementType;
import com.pedronunesdev.MenteFinanceira.mappers.movement.MovementMapper;
import com.pedronunesdev.MenteFinanceira.repositories.movement.MovementRepository;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import com.pedronunesdev.MenteFinanceira.services.wallet.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    private static final Long USER_ID = 1L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Mock
    private MovementRepository movementRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private MovementMapper movementMapper;
    @Mock
    private WalletService walletService;
    @InjectMocks
    private MovementService movementService;

    @Nested
    @DisplayName("Registro de movimentação")
    class RegisterMovement {

        @Test
        @DisplayName("Deve montar a movimentação com os dados recebidos, salvar e devolver os dados salvos")
        void shouldBuildSaveAndReturnMovement() {
            Wallet wallet = Wallet.builder().walletId(10L).balance(new BigDecimal("500.00")).build();
            LocalDateTime executionDate = LocalDateTime.of(2026, 11, 17, 14, 30);

            when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> {
                Movement toSave = invocation.getArgument(0);
                toSave.setMovementId(99L);
                toSave.setExecutionDate(executionDate);
                return toSave;
            });

            MovementResponse response = movementService.registerMovement(
                    new BigDecimal("150.00"), "Compras da semana", MovementType.RETIRADA, "mercado", wallet);

            ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
            verify(movementRepository).save(captor.capture());
            Movement saved = captor.getValue();

            assertThat(saved.getMovedAmount()).isEqualByComparingTo("150.00");
            assertThat(saved.getDescription()).isEqualTo("Compras da semana");
            assertThat(saved.getMovementType()).isEqualTo(MovementType.RETIRADA);
            assertThat(saved.getMovementCategory()).isEqualTo(MovementCategory.MERCADO);
            assertThat(saved.getWallet()).isSameAs(wallet);

            assertThat(response.movementId()).isEqualTo(99L);
            assertThat(response.movedAmount()).isEqualByComparingTo("150.00");
            assertThat(response.executionDate()).isEqualTo(executionDate);
            assertThat(response.movementType()).isEqualTo(MovementType.RETIRADA);
            assertThat(response.movementCategory()).isEqualTo(MovementCategory.MERCADO);
        }

        @Test
        @DisplayName("Deve rejeitar categoria inexistente sem salvar nada")
        void shouldRejectUnknownCategory() {
            Wallet wallet = Wallet.builder().walletId(10L).balance(BigDecimal.TEN).build();

            assertThatThrownBy(() -> movementService.registerMovement(
                    BigDecimal.TEN, "Teste", MovementType.ENTRADA, "CATEGORIA_QUE_NAO_EXISTE", wallet))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(movementRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Histórico de movimentações")
    class FindMovementHistory {

        @Test
        @DisplayName("Sem datas, deve devolver o histórico completo do usuário")
        void shouldReturnFullHistoryWhenNoDatesAreGiven() {
            Movement movement = Movement.builder().movementId(1L).build();
            MovementResponse dto = new MovementResponse(1L, BigDecimal.TEN, null, MovementType.ENTRADA, MovementCategory.SALARIO);

            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.findAllMovementsByUser(USER_ID, PAGEABLE))
                    .thenReturn(new PageImpl<>(List.of(movement), PAGEABLE, 1));
            when(movementMapper.toDto(movement)).thenReturn(dto);

            Page<MovementResponse> result = movementService.findMovementHistory(null, null, PAGEABLE);

            assertThat(result.getContent()).containsExactly(dto);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(movementRepository, never()).findMovementsByDate(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Com só a data inicial, deve lançar erro sem consultar o banco")
        void shouldRejectWhenOnlyInitialDateIsGiven() {
            assertThatThrownBy(() -> movementService.findMovementHistory(LocalDate.of(2026, 11, 1), null, PAGEABLE))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(movementRepository);
        }

        @Test
        @DisplayName("Com só a data final, deve lançar erro sem consultar o banco")
        void shouldRejectWhenOnlyFinalDateIsGiven() {
            assertThatThrownBy(() -> movementService.findMovementHistory(null, LocalDate.of(2026, 11, 30), PAGEABLE))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(movementRepository);
        }

        @Test
        @DisplayName("Com a data inicial posterior à final, deve lançar erro sem consultar o banco")
        void shouldRejectWhenInitialDateIsAfterFinalDate() {
            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);

            assertThatThrownBy(() -> movementService.findMovementHistory(
                    LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 17), PAGEABLE))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(movementRepository);
        }

        @Test
        @DisplayName("Com período válido, deve buscar de meia-noite do início até meia-noite do dia seguinte ao fim")
        void shouldQueryHalfOpenIntervalForValidPeriod() {
            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.findMovementsByDate(any(), any(), any(), any()))
                    .thenReturn(Page.empty(PAGEABLE));

            movementService.findMovementHistory(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), PAGEABLE);

            verify(movementRepository).findMovementsByDate(
                    USER_ID,
                    LocalDateTime.of(2026, 11, 1, 0, 0),
                    LocalDateTime.of(2026, 12, 1, 0, 0),
                    PAGEABLE);
            verify(movementRepository, never()).findAllMovementsByUser(any(), any());
        }

        @Test
        @DisplayName("Com início e fim no mesmo dia, deve cobrir o dia inteiro")
        void shouldCoverTheWholeDayWhenInitialAndFinalAreTheSame() {
            LocalDate day = LocalDate.of(2026, 11, 17);

            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.findMovementsByDate(any(), any(), any(), any()))
                    .thenReturn(Page.empty(PAGEABLE));

            movementService.findMovementHistory(day, day, PAGEABLE);

            verify(movementRepository).findMovementsByDate(
                    USER_ID,
                    LocalDateTime.of(2026, 11, 17, 0, 0),
                    LocalDateTime.of(2026, 11, 18, 0, 0),
                    PAGEABLE);
        }

        @Test
        @DisplayName("Deve converter cada movimentação da página em DTO")
        void shouldMapEveryMovementOfThePage() {
            Movement first = Movement.builder().movementId(1L).build();
            Movement second = Movement.builder().movementId(2L).build();
            MovementResponse firstDto = new MovementResponse(1L, BigDecimal.ONE, null, MovementType.ENTRADA, MovementCategory.SALARIO);
            MovementResponse secondDto = new MovementResponse(2L, BigDecimal.TWO, null, MovementType.RETIRADA, MovementCategory.LAZER);

            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.findMovementsByDate(any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(first, second), PAGEABLE, 2));
            when(movementMapper.toDto(first)).thenReturn(firstDto);
            when(movementMapper.toDto(second)).thenReturn(secondDto);

            Page<MovementResponse> result = movementService.findMovementHistory(
                    LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), PAGEABLE);

            assertThat(result.getContent()).containsExactly(firstDto, secondDto);
        }
    }

    @Nested
    @DisplayName("Análise de movimentações por período")
    class AnalyzeMovements {

        private static final LocalDate START = LocalDate.of(2026, 11, 1);
        private static final LocalDate END = LocalDate.of(2026, 11, 30);

        @Test
        @DisplayName("Com a data inicial posterior à final, deve lançar erro sem consultar nada")
        void shouldRejectWhenInitialDateIsAfterFinalDate() {
            assertThatThrownBy(() -> movementService.analyzeMovementsByMonth(
                    LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 17)))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(movementRepository, walletService);
        }

        @Test
        @DisplayName("Deve consultar os totais no intervalo [início 00:00, dia seguinte ao fim 00:00)")
        void shouldQueryHalfOpenInterval() {
            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.totalByCategory(any(), any(), any())).thenReturn(List.of());
            when(walletService.getUserBalance()).thenReturn(new BalanceResponse(BigDecimal.ZERO));

            movementService.analyzeMovementsByMonth(START, END);

            verify(movementRepository).totalByCategory(
                    USER_ID,
                    LocalDateTime.of(2026, 11, 1, 0, 0),
                    LocalDateTime.of(2026, 12, 1, 0, 0));
        }

        @Test
        @DisplayName("Deve somar entradas e retiradas e calcular o percentual de cada categoria dentro do seu tipo")
        void shouldCalculateTotalsAndPercentagesByType() {
            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.totalByCategory(any(), any(), any())).thenReturn(List.of(
                    new TotalByCategory(MovementCategory.SALARIO, MovementType.ENTRADA, new BigDecimal("3000.00")),
                    new TotalByCategory(MovementCategory.RENDIMENTOS, MovementType.ENTRADA, new BigDecimal("1000.00")),
                    new TotalByCategory(MovementCategory.MERCADO, MovementType.RETIRADA, new BigDecimal("600.00")),
                    new TotalByCategory(MovementCategory.LAZER, MovementType.RETIRADA, new BigDecimal("400.00"))
            ));
            when(walletService.getUserBalance()).thenReturn(new BalanceResponse(new BigDecimal("3000.00")));

            AnalysisMovementCategory analysis = movementService.analyzeMovementsByMonth(START, END);

            assertThat(analysis.totalAmount()).isEqualByComparingTo("5000.00");
            assertThat(analysis.income()).isEqualByComparingTo("4000.00");
            assertThat(analysis.withdrawal()).isEqualByComparingTo("1000.00");
            assertThat(analysis.currentBalance()).isEqualByComparingTo("3000.00");

            assertThat(percentageOf(analysis, MovementCategory.SALARIO)).isEqualByComparingTo("75.00");
            assertThat(percentageOf(analysis, MovementCategory.RENDIMENTOS)).isEqualByComparingTo("25.00");
            assertThat(percentageOf(analysis, MovementCategory.MERCADO)).isEqualByComparingTo("60.00");
            assertThat(percentageOf(analysis, MovementCategory.LAZER)).isEqualByComparingTo("40.00");
        }

        @Test
        @DisplayName("Deve arredondar o percentual com 2 casas, HALF_UP")
        void shouldRoundPercentageHalfUp() {
            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.totalByCategory(any(), any(), any())).thenReturn(List.of(
                    new TotalByCategory(MovementCategory.MERCADO, MovementType.RETIRADA, new BigDecimal("200.00")),
                    new TotalByCategory(MovementCategory.LAZER, MovementType.RETIRADA, new BigDecimal("100.00"))
            ));
            when(walletService.getUserBalance()).thenReturn(new BalanceResponse(BigDecimal.ZERO));

            AnalysisMovementCategory analysis = movementService.analyzeMovementsByMonth(START, END);

            assertThat(percentageOf(analysis, MovementCategory.MERCADO)).isEqualTo(new BigDecimal("66.67"));
            assertThat(percentageOf(analysis, MovementCategory.LAZER)).isEqualTo(new BigDecimal("33.33"));
        }

        @Test
        @DisplayName("Sem movimentações no período, deve devolver totais zerados e o saldo atual")
        void shouldReturnZeroTotalsWhenThereAreNoMovements() {
            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.totalByCategory(any(), any(), any())).thenReturn(List.of());
            when(walletService.getUserBalance()).thenReturn(new BalanceResponse(new BigDecimal("250.00")));

            AnalysisMovementCategory analysis = movementService.analyzeMovementsByMonth(START, END);

            assertThat(analysis.totalAmount()).isEqualByComparingTo("0");
            assertThat(analysis.income()).isEqualByComparingTo("0");
            assertThat(analysis.withdrawal()).isEqualByComparingTo("0");
            assertThat(analysis.currentBalance()).isEqualByComparingTo("250.00");
            assertThat(analysis.categoryBreakdown()).isEmpty();
        }

        @Test
        @DisplayName("Quando o total do tipo é zero, deve definir o percentual como 0 em vez de dividir por zero")
        void shouldNotDivideByZeroWhenTypeTotalIsZero() {
            when(authenticationService.extractAuthenticatedUserId()).thenReturn(USER_ID);
            when(movementRepository.totalByCategory(any(), any(), any())).thenReturn(List.of(
                    new TotalByCategory(MovementCategory.SALARIO, MovementType.ENTRADA, BigDecimal.ZERO)
            ));
            when(walletService.getUserBalance()).thenReturn(new BalanceResponse(BigDecimal.ZERO));

            AnalysisMovementCategory analysis = movementService.analyzeMovementsByMonth(START, END);

            assertThat(percentageOf(analysis, MovementCategory.SALARIO)).isEqualByComparingTo("0");
        }

        private BigDecimal percentageOf(AnalysisMovementCategory analysis, MovementCategory category) {
            return analysis.categoryBreakdown().stream()
                    .filter(item -> item.category() == category)
                    .map(PercentageMovementCategory::percentage)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Categoria " + category + " não encontrada na análise"));
        }
    }
}
