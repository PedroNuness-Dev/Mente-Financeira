package com.pedronunesdev.MenteFinanceira.exception;

import java.time.LocalDateTime;

public record ExceptionResponse(
        LocalDateTime timestamp,
        Integer status,
        String messagem,
        String details
) {
}
