package com.pedronunesdev.MenteFinanceira.security;

import com.pedronunesdev.MenteFinanceira.dto.auth.JWTCreateResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JWTService {

    @Value("${api.security.token.secret}")
    private String keySecret;
    @Value("${api.security.token.issuer}")
    private String issuer;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(keySecret.getBytes(StandardCharsets.UTF_8));
    }

    public JWTCreateResponse gerarJWT(UserDetailsImpl userDetails){

        Instant agora = Instant.now();
        Instant expiracao = agora.plus(2, ChronoUnit.HOURS);

        String token = Jwts.builder()
                .issuer(issuer)
                .signWith(getSigningKey())
                .subject(userDetails.getUsername()) // Email do usuário
                .claim("id", userDetails.getId())
                .claim("nome", userDetails.getNome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .compact();

        return new JWTCreateResponse(expiracao,token);
    }

    public String validarJWTEObterSubject(String token){

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean isJWTValido(String token){
        try{
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        }
        catch (ExpiredJwtException e){
            return false;
        }
        catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }
}
