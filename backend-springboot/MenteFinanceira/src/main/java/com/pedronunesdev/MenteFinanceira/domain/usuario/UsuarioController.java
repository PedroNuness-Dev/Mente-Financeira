package com.pedronunesdev.MenteFinanceira.domain.usuario;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuario")
@Tag(name = "Usuario Controller", description = "Responsável por todas as ações relacionadas diretamente com o usuário")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioDTOResponse> cadastrarUsuario(@RequestBody @Valid UsuarioDTORequest request){

        UsuarioDTOResponse response = usuarioService.cadastrarUsuario(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
