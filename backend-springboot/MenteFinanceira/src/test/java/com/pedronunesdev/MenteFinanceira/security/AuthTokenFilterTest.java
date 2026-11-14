package com.pedronunesdev.MenteFinanceira.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    private static final String TOKEN = "header.payload.signature";
    private static final String EMAIL = "pedro@teste.com";

    @Mock
    private TokenService tokenService;
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    @InjectMocks
    private AuthTokenFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sem header Authorization: segue a cadeia sem autenticar e sem validar token")
    void shouldContinueAnonymouslyWithoutAuthorizationHeader() throws ServletException, IOException {
        filter.doFilter(request, response, chain);

        assertAnonymousAndChainContinued();
        verify(tokenService, never()).validateTokenAndGetSubject(anyString());
    }

    @Test
    @DisplayName("Header 'Bearer ' vazio: segue a cadeia sem autenticar e sem validar token")
    void shouldContinueAnonymouslyWithBlankBearer() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer   ");

        filter.doFilter(request, response, chain);

        assertAnonymousAndChainContinued();
        verify(tokenService, never()).validateTokenAndGetSubject(anyString());
    }

    @Test
    @DisplayName("Header sem prefixo Bearer: é ignorado")
    void shouldIgnoreNonBearerHeader() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic dXNlcjpzZW5oYQ==");

        filter.doFilter(request, response, chain);

        assertAnonymousAndChainContinued();
        verify(tokenService, never()).validateTokenAndGetSubject(anyString());
    }

    @Test
    @DisplayName("Token inválido: segue a cadeia sem autenticar e sem consultar o usuário")
    void shouldContinueAnonymouslyWithInvalidToken() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(tokenService.validateTokenAndGetSubject(TOKEN)).thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        assertAnonymousAndChainContinued();
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Token válido de usuário excluído: não lança exceção e segue anônimo")
    void shouldContinueAnonymouslyWhenUserNoLongerExists() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        when(tokenService.validateTokenAndGetSubject(TOKEN)).thenReturn(Optional.of(EMAIL));
        when(userDetailsService.loadUserByUsername(EMAIL)).thenThrow(new UsernameNotFoundException("Credenciais inválidas"));

        filter.doFilter(request, response, chain);

        assertAnonymousAndChainContinued();
    }

    @Test
    @DisplayName("Token válido: autentica o usuário com suas authorities e segue a cadeia")
    void shouldAuthenticateWithValidToken() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + TOKEN);
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "Pedro", EMAIL, "senha-hash", 2026, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(tokenService.validateTokenAndGetSubject(TOKEN)).thenReturn(Optional.of(EMAIL));
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isSameAs(userDetails);
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(chain.getRequest()).isSameAs(request);
    }

    private void assertAnonymousAndChainContinued() {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest())
                .as("o filtro deve sempre chamar filterChain.doFilter")
                .isSameAs(request);
    }
}
