package com.pedronunesdev.MenteFinanceira.dto.auth;

import java.time.Instant;

public record LoginResponseDTO(
        String token,
        String type, // "Bearer"
        Instant expiresAt,
        Long userId,
        String name,
        String email
) {
}