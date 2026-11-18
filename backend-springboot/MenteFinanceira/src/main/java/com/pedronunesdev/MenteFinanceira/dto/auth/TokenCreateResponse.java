package com.pedronunesdev.MenteFinanceira.dto.auth;

import java.time.Instant;

public record TokenCreateResponse(
        Instant expiration,
        String token
) {
}