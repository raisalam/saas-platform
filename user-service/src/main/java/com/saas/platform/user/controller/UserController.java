package com.saas.platform.user.controller;

import com.saas.platform.user.dto.*;
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
        System.out.println("===========================inside register controller=======");
        System.out.println(req);

        return ResponseEntity.ok(
               userService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest dto) {

        System.out.println("===========================inside controller=======");
        System.out.println(dto.toString());
        TokenResponse tokenResponse = userService.login(dto);
        return ResponseEntity.ok(tokenResponse);
    }


    @PostMapping("/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse user = userService.getProfile(userId);
        return ResponseEntity.ok(user);
    }

    // Update user balance
    @PostMapping("/{userId}/balance")
    public ResponseEntity<UserResponse> updateBalance(
            @PathVariable Long userId,
            @RequestBody UpdateBalanceRequest request) {
        UserResponse updatedUser = userService.updateBalance(userId, request.getTotalCost());
        return ResponseEntity.ok(updatedUser);
    }
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestParam String refreshToken) {
        TokenResponse tokenResponse = userService.refresh(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

}
