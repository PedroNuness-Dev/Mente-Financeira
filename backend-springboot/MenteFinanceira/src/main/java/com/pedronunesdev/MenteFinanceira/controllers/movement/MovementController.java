package com.pedronunesdev.MenteFinanceira.controllers.movement;

import com.pedronunesdev.MenteFinanceira.dto.movement.MovementCategoryAnalysisDTOResponse;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementDTOResponse;
import com.pedronunesdev.MenteFinanceira.services.movement.MovementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/wallet/movements")
@Tag(name = "Movement Controller", description = "Responsible for all actions related to the user's movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;

    @GetMapping
    public ResponseEntity<Page<MovementDTOResponse>> findMovementHistory(Pageable pageable){

        Page<MovementDTOResponse> page = movementService.findMovementHistory(pageable);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{month}/{year}")
    public ResponseEntity<MovementCategoryAnalysisDTOResponse> findPercentagesByMovementCategory(
            @PathVariable Integer month, @PathVariable Integer year){

        MovementCategoryAnalysisDTOResponse response = movementService.analyzeMovementsByMonth(month, year);

        return ResponseEntity.ok(response);
    }
}