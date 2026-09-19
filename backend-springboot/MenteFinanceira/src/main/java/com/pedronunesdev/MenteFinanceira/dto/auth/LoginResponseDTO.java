package com.pedronunesdev.MenteFinanceira.dto.auth;

import java.time.Instant;

public record LoginResponseDTO(
        String token,
        String tipo, // "Bearer"
        Instant expiraEm,
        Long idUsuario,
        String nome,
        String email
) {
}
