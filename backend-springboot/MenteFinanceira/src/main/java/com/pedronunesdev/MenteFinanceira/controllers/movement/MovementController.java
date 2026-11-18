package com.pedronunesdev.MenteFinanceira.controllers.movement;

import com.pedronunesdev.MenteFinanceira.dto.movement.AnalysisMovementCategory;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementResponse;
import com.pedronunesdev.MenteFinanceira.services.movement.MovementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/user/wallet/movements")
@Tag(name = "Movement Controller", description = "Responsible for all actions related to the user's movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;

    @GetMapping
    public ResponseEntity<PagedModel<MovementResponse>> findMovementHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate initialDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finalDate,
            @PageableDefault(size = 20, sort = "executionDate", direction = Sort.Direction.ASC) Pageable pageable){

        Page<MovementResponse> movementHistory = movementService.findMovementHistory(initialDate,finalDate,pageable);

        return ResponseEntity.ok(new PagedModel<>(movementHistory));
    }

    @GetMapping("/analysis")
    public ResponseEntity<AnalysisMovementCategory> findPercentagesByMovementCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate initialDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finalDate){

        AnalysisMovementCategory response = movementService.analyzeMovementsByMonth(initialDate, finalDate);

        return ResponseEntity.ok(response);
    }
}