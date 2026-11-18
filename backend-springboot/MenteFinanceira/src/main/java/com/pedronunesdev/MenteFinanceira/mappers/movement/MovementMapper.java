package com.pedronunesdev.MenteFinanceira.mappers.movement;

import com.pedronunesdev.MenteFinanceira.domain.movement.Movement;
import com.pedronunesdev.MenteFinanceira.dto.movement.MovementResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovementMapper {

    MovementResponse toDto(Movement movement);
}
