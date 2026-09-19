package com.pedronunesdev.MenteFinanceira.services.usuario;

import com.pedronunesdev.MenteFinanceira.domain.role.Role;
import com.pedronunesdev.MenteFinanceira.domain.usuario.Usuario;
import com.pedronunesdev.MenteFinanceira.dto.usuario.UsuarioDTORequest;
import com.pedronunesdev.MenteFinanceira.dto.usuario.UsuarioDTOResponse;
import com.pedronunesdev.MenteFinanceira.enums.role.EnumRole;
import com.pedronunesdev.MenteFinanceira.exception.ResourceNotFoundException;
import com.pedronunesdev.MenteFinanceira.repositories.role.RoleRepository;
import com.pedronunesdev.MenteFinanceira.repositories.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public UsuarioDTOResponse cadastrarUsuario(UsuarioDTORequest request){

        if (usuarioRepository.findByEmail(request.email()).isPresent()) throw new IllegalArgumentException("Erro ao cadastrar usuário");

        log.info("Iniciando cadastro de usuário com email: [{}]", request.email());

        Role roleUsuario = roleRepository.findByNome(EnumRole.ROLE_USUARIO)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada"));

        Usuario usuarioParaSalvar = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(bCryptPasswordEncoder.encode(request.senha()))
                .build();

        //Seta a Collection de Role para a de um usuário padrão
        usuarioParaSalvar.setRoles(Set.of(roleUsuario));

        usuarioRepository.save(usuarioParaSalvar);

        log.info("Usuário salvo com sucesso, com ID: [{}]", usuarioParaSalvar.getId());

        return new UsuarioDTOResponse(
                usuarioParaSalvar.getId(),
                usuarioParaSalvar.getNome(),
                usuarioParaSalvar.getEmail()
        );
    }
}
