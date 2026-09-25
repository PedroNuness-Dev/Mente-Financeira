package com.pedronunesdev.MenteFinanceira.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserDTOResponse(
        Long id,
        String name,
        String email,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime creationDate,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime updateDate,
        Boolean firstLogin
) {
}