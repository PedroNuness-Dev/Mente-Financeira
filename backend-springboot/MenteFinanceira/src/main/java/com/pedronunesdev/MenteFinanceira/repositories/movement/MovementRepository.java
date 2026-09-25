package com.pedronunesdev.MenteFinanceira.repositories.movement;

import com.pedronunesdev.MenteFinanceira.dto.movement.CategoryTotalDTO;
import com.pedronunesdev.MenteFinanceira.domain.movement.Movement;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementDTOResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovementRepository extends JpaRepository<Movement,Long> {

    @Query("""
    SELECT m FROM Movement m
    WHERE m.wallet.user.id = :id
    ORDER BY m.executionDate DESC
""")
    Page<MovementDTOResponse> movementHistory(@Param("id") Long userId, Pageable pageable);

    @Query("""
    SELECT
        m.movementCategory,
        m.movementType,
        SUM(m.movedAmount)
    FROM Movement m
    WHERE m.wallet.user.id = :id
    AND m.executionDate BETWEEN :firstDay AND :lastDay
    GROUP BY m.movementCategory, m.movementType
    ORDER BY SUM(m.movedAmount) DESC
""")
    List<CategoryTotalDTO> totalByCategory(@Param("id") Long userId, @Param("firstDay") LocalDateTime firstDay, @Param("lastDay") LocalDateTime lastDay);
}