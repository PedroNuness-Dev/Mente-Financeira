package com.pedronunesdev.MenteFinanceira.services.auth;

import com.pedronunesdev.MenteFinanceira.domain.usuario.Usuario;
import com.pedronunesdev.MenteFinanceira.dto.auth.JWTCreateResponse;
import com.pedronunesdev.MenteFinanceira.dto.auth.LoginRequestDTO;
import com.pedronunesdev.MenteFinanceira.dto.auth.LoginResponseDTO;
import com.pedronunesdev.MenteFinanceira.exception.ResourceNotFoundException;
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
    private final UsuarioRepository usuarioRepository;

    public LoginResponseDTO autenticarUsuario(LoginRequestDTO requestDTO){

        log.info("Iniciando processo de autenticação do usuário com email [{}]", requestDTO.email());

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(requestDTO.email(),requestDTO.senha()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) Objects.requireNonNull(authentication.getPrincipal());

        JWTCreateResponse tokenJWT = jwtService.gerarJWT(userDetails);

        log.info("Autenticação realizada com sucesso");

        return new LoginResponseDTO(
                tokenJWT.token(),
                "Bearer",
                tokenJWT.expiracao(),
                userDetails.getId(),
                userDetails.getNome(),
                userDetails.getEmail()
        );
    }

    public Usuario me(){

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return usuarioRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
    }

    public Long extrairIdDoUsuarioAutenticado(){

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getId();
    }

    public int extrairAnoCriacaoUsuarioAutenticado(){

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userDetails.getAnoCriacaoUsuario();
    }
}
