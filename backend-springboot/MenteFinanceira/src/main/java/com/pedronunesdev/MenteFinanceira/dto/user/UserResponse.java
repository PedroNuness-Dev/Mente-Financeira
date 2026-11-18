package com.pedronunesdev.MenteFinanceira.dto.user;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime creationDate,
        LocalDateTime updateDate
) {
}