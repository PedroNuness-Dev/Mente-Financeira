package com.pedronunesdev.MenteFinanceira.security;

import com.pedronunesdev.MenteFinanceira.dto.auth.JWTCreateResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class TokenService {

    private final SecretKey signingKey;
    private final String issuer;

    public TokenService(TokenProperties tokenProperties){
        signingKey = Keys.hmacShaKeyFor(tokenProperties.secret().getBytes(StandardCharsets.UTF_8));
        issuer = tokenProperties.issuer();
    }

    public JWTCreateResponse generateToken(UserDetailsImpl userDetails){

        Instant now = Instant.now();
        Instant expiration = now.plus(2, ChronoUnit.HOURS);

        String token = Jwts.builder()
                .issuer(issuer)
                .signWith(signingKey)
                .subject(userDetails.getUsername()) // User's email
                .claim("id", userDetails.getId())
                .claim("name", userDetails.getName())
                .claim("creationYear", userDetails.getUserCreationYear())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .compact();

        return new JWTCreateResponse(expiration, token);
    }

    public Optional<String> validateTokenAndGetSubject(String token){

        try {
            Claims claims = Jwts.parser()
                    .requireIssuer(issuer)
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.ofNullable(claims.getSubject());
        }catch (JwtException | IllegalArgumentException e){

            log.warn("Token inválido: {}", e.getMessage());

            return Optional.empty();
        }
    }
}