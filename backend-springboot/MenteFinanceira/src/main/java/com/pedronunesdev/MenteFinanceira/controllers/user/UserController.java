package com.pedronunesdev.MenteFinanceira.controllers.user;

import com.pedronunesdev.MenteFinanceira.dto.user.UserRequest;
import com.pedronunesdev.MenteFinanceira.dto.user.UserResponse;
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

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest request){

        UserResponse response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> findUserProfile(){

        UserResponse userProfile = userService.findUserProfile();

        return ResponseEntity.ok(userProfile);
    }
}