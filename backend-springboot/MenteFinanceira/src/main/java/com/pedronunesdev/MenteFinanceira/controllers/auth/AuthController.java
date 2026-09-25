package com.pedronunesdev.MenteFinanceira.controllers.auth;

import com.pedronunesdev.MenteFinanceira.dto.auth.LoginRequestDTO;
import com.pedronunesdev.MenteFinanceira.dto.auth.LoginResponseDTO;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Responsible for the user authentication flow")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO requestDTO){

        LoginResponseDTO loginResponseDTO = authenticationService.authenticateUser(requestDTO);

        return ResponseEntity.ok(loginResponseDTO);
    }
}