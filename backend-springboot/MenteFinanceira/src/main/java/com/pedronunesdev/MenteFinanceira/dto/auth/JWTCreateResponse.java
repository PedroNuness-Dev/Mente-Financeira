package com.pedronunesdev.MenteFinanceira.dto.auth;

import java.time.Instant;

public record JWTCreateResponse(
        Instant expiration,
        String token
) {
}