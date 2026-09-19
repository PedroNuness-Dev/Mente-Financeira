package com.pedronunesdev.MenteFinanceira.services.auth;

import com.pedronunesdev.MenteFinanceira.dto.auth.JWTCreateResponse;
import com.pedronunesdev.MenteFinanceira.dto.auth.LoginRequestDTO;
import com.pedronunesdev.MenteFinanceira.dto.auth.LoginResponseDTO;
import com.pedronunesdev.MenteFinanceira.repositories.usuario.UsuarioRepository;
import com.pedronunesdev.MenteFinanceira.security.JWTService;
import com.pedronunesdev.MenteFinanceira.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDTO autenticarUsuario(LoginRequestDTO requestDTO){

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(requestDTO.email(),requestDTO.senha()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) Objects.requireNonNull(authentication.getPrincipal());

        JWTCreateResponse tokenJWT = jwtService.gerarJWT(userDetails);

        return new LoginResponseDTO(
                tokenJWT.token(),
                "Bearer",
                tokenJWT.expiracao(),
                userDetails.getId(),
                userDetails.getNome(),
                userDetails.getEmail()
        );
    }
}
