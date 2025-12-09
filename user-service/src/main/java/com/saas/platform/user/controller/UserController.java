package com.saas.platform.user.controller;

import com.saas.platform.user.dto.LoginRequest;
import com.saas.platform.user.dto.RegisterRequest;
import com.saas.platform.user.dto.TokenResponse;
import com.saas.platform.user.dto.UserResponse;
import com.saas.platform.user.mapper.UserMapper;
import com.saas.platform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(
                UserMapper.INSTANCE.toResponse(userService.register(req))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest dto) {
        TokenResponse tokenResponse = userService.login(dto);
        return ResponseEntity.ok(tokenResponse);
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestParam String refreshToken) {
        TokenResponse tokenResponse = userService.refresh(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

}
