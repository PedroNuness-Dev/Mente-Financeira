package com.pedronunesdev.MenteFinanceira.controllers.user;

import com.pedronunesdev.MenteFinanceira.dto.user.UserDTORequest;
import com.pedronunesdev.MenteFinanceira.dto.user.UserDTOResponse;
import com.pedronunesdev.MenteFinanceira.services.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Responsible for all actions directly related to the user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTOResponse> registerUser(@RequestBody @Valid UserDTORequest request){

        UserDTOResponse response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTOResponse> findUserProfile(){

        UserDTOResponse userProfile = userService.findUserProfile();

        return ResponseEntity.ok(userProfile);
    }
}