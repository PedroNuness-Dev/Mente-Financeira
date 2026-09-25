package com.pedronunesdev.MenteFinanceira.services.user;

import com.pedronunesdev.MenteFinanceira.domain.role.Role;
import com.pedronunesdev.MenteFinanceira.domain.user.User;
import com.pedronunesdev.MenteFinanceira.dto.user.UserDTORequest;
import com.pedronunesdev.MenteFinanceira.dto.user.UserDTOResponse;
import com.pedronunesdev.MenteFinanceira.enums.role.RoleType;
import com.pedronunesdev.MenteFinanceira.exception.ResourceNotFoundException;
import com.pedronunesdev.MenteFinanceira.repositories.role.RoleRepository;
import com.pedronunesdev.MenteFinanceira.repositories.user.UserRepository;
import com.pedronunesdev.MenteFinanceira.services.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationService authenticationService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public UserDTOResponse registerUser(UserDTORequest request){

        if (userRepository.findByEmail(request.email()).isPresent()) throw new IllegalArgumentException("Error registering user");

        log.info("Starting user registration with email: [{}]", request.email());

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User userToSave = User.builder()
                .name(request.name())
                .email(request.email())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .build();

        //Sets the Role collection for a default user
        userToSave.setRoles(Set.of(userRole));

        userRepository.save(userToSave);

        log.info("User saved successfully, with ID: [{}]", userToSave.getId());

        return new UserDTOResponse(
                userToSave.getId(),
                userToSave.getName(),
                userToSave.getEmail(),
                userToSave.getCreationDate(),
                userToSave.getUpdateDate(),
                userToSave.getFirstLogin()
        );
    }

    public UserDTOResponse findUserProfile(){

        log.info("Retrieving authenticated user profile");

        User userAuthenticated = authenticationService.me();

        return new UserDTOResponse(
            userAuthenticated.getId(),
            userAuthenticated.getName(),
            userAuthenticated.getEmail(),
            userAuthenticated.getCreationDate(),
            userAuthenticated.getUpdateDate(),
            userAuthenticated.getFirstLogin()
        );
    }
}