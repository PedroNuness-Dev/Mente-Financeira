package com.pedronunesdev.MenteFinanceira.controllers.movement;

import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import com.pedronunesdev.MenteFinanceira.services.movement.MovementCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/movements/categories")
@RequiredArgsConstructor
@Tag(name = "Movement Category Controller", description = "Responsible for actions related to movement categories")
public class MovementCategoryController {

    private final MovementCategoryService movementCategoryService;

    @Operation(summary = "Find all movements",
            description = "Fetches all movements registered in the system")
    @GetMapping
    public ResponseEntity<List<MovementCategory>> findAllMovementCategories(){

        List<MovementCategory> responses = movementCategoryService.findAllMovementCategories();

        return ResponseEntity.ok(responses);
    }
}