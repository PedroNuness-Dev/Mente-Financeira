package com.pedronunesdev.MenteFinanceira.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserDTOResponse(
        Long id,
        String name,
        String email,
        LocalDateTime creationDate,
        LocalDateTime updateDate
) {
}