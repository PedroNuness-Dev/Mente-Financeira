package com.pedronunesdev.MenteFinanceira.security;

import com.pedronunesdev.MenteFinanceira.dto.auth.TokenCreateResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SECRET = "chave-fake-apenas-para-testes-unitarios-com-mais-de-64-bytes-0123456789";
    private static final String OTHER_SECRET = "outra-chave-fake-que-nao-e-a-da-aplicacao-com-mais-de-64-bytes-9876543210";
    private static final String ISSUER = "mentefinanceira-test";
    private static final String EMAIL = "pedro@teste.com";

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(new TokenProperties(SECRET, ISSUER));
    }

    @Test
    @DisplayName("Deve falhar na criação do serviço quando a chave é curta demais para HMAC")
    void shouldFailFastWithWeakKey() {
        assertThatThrownBy(() -> new TokenService(new TokenProperties("abc", ISSUER)))
                .isInstanceOf(WeakKeyException.class);
    }

    @Test
    @DisplayName("Deve gerar um token cujo subject é o e-mail do usuário")
    void shouldGenerateTokenWithEmailAsSubject() {
        TokenCreateResponse response = tokenService.generateToken(buildUserDetails());

        assertThat(tokenService.validateTokenAndGetSubject(response.token())).contains(EMAIL);
    }

    @Test
    @DisplayName("Deve expirar o token 2 horas após a geração")
    void shouldExpireTokenAfterTwoHours() {
        Instant before = Instant.now();

        TokenCreateResponse response = tokenService.generateToken(buildUserDetails());

        assertThat(response.expiration())
                .isAfterOrEqualTo(before.plus(2, ChronoUnit.HOURS))
                .isBefore(before.plus(2, ChronoUnit.HOURS).plusSeconds(5));
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void shouldRejectExpiredToken() {
        String token = Jwts.builder()
                .issuer(ISSUER)
                .subject(EMAIL)
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                .signWith(key(SECRET))
                .compact();

        assertThat(tokenService.validateTokenAndGetSubject(token)).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar token assinado com outra chave")
    void shouldRejectTokenSignedWithAnotherKey() {
        String token = validTokenBuilder().signWith(key(OTHER_SECRET)).compact();

        assertThat(tokenService.validateTokenAndGetSubject(token)).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar token emitido por outro issuer")
    void shouldRejectTokenFromAnotherIssuer() {
        String token = validTokenBuilder().issuer("outra-api").signWith(key(SECRET)).compact();

        assertThat(tokenService.validateTokenAndGetSubject(token)).isEmpty();
    }

    @Test
    @DisplayName("Deve devolver vazio (sem NullPointerException) para token válido sem subject")
    void shouldReturnEmptyWhenTokenHasNoSubject() {
        String token = Jwts.builder()
                .issuer(ISSUER)
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key(SECRET))
                .compact();

        assertThat(tokenService.validateTokenAndGetSubject(token)).isEmpty();
    }

    @ParameterizedTest(name = "token = \"{0}\"")
    @ValueSource(strings = {"", "   ", "token-invalido", "a.b.c"})
    @DisplayName("Deve rejeitar tokens vazios ou malformados sem lançar exceção")
    void shouldRejectBlankOrMalformedTokens(String token) {
        assertThat(tokenService.validateTokenAndGetSubject(token)).isEmpty();
    }

    private io.jsonwebtoken.JwtBuilder validTokenBuilder() {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(EMAIL)
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static UserDetailsImpl buildUserDetails() {
        return new UserDetailsImpl(1L, "Pedro", EMAIL, "senha-hash", 2026, List.of());
    }
}
