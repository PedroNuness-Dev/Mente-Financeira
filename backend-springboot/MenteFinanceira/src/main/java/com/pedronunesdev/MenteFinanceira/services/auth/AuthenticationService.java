package com.pedronunesdev.MenteFinanceira.services.auth;

import com.pedronunesdev.MenteFinanceira.domain.user.User;
import com.pedronunesdev.MenteFinanceira.dto.auth.LoginRequestDTO;
import com.pedronunesdev.MenteFinanceira.dto.auth.LoginResponseDTO;
import com.pedronunesdev.MenteFinanceira.dto.auth.TokenCreateResponse;
import com.pedronunesdev.MenteFinanceira.exception.ResourceNotFoundException;
import com.pedronunesdev.MenteFinanceira.repositories.user.UserRepository;
import com.pedronunesdev.MenteFinanceira.security.TokenService;
import com.pedronunesdev.MenteFinanceira.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public LoginResponseDTO authenticateUser(LoginRequestDTO requestDTO){

        log.info("Starting authentication process for user with email [{}]", requestDTO.email());

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(requestDTO.email(),requestDTO.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) Objects.requireNonNull(authentication.getPrincipal());

        TokenCreateResponse tokenCreated = tokenService.generateToken(userDetails);

        log.info("Authentication completed successfully");

        return new LoginResponseDTO(
                tokenCreated.token(),
                "Bearer",
                tokenCreated.expiration(),
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail()
        );
    }

    public User me() {


        UserDetailsImpl userDetails = getUserDetailsImpl();

        return userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"
                ));
    }

    public Long extractAuthenticatedUserId(){

        UserDetailsImpl userDetails = getUserDetailsImpl();

        return userDetails.getId();
    }

    private UserDetailsImpl getUserDetailsImpl(){
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Usuário não autenticado"
            );
        }

        return (UserDetailsImpl) authentication.getPrincipal();
    }
}