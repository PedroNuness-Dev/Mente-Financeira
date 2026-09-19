package com.pedronunesdev.MenteFinanceira.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ExceptionResponse(
        @JsonFormat(pattern = "dd/MM/yyyy HH:ss:mm")
        LocalDateTime timestamp,
        Integer status,
        String messagem,
        String details
) {
}
