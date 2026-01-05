package com.saas.platform.user.controller;

import com.saas.platform.user.dto.*;
import com.saas.platform.user.mapper.UserMapper;
import com.saas.platform.user.service.UserActivityService;
import com.saas.platform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserActivityService userActivityService;

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

    @PostMapping("/activity")
    public ResponseEntity<List<UserActivityDto>> activity() {
        return ResponseEntity.ok(userActivityService.recentActivities(Long.valueOf(MDC.get("sellerId"))));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse user = userService.getProfile(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/names")
    public ResponseEntity<Map<Long, String>> getUsernames(@RequestParam List<Long> ids) {
        // Log for debugging internal microservice calls
        log.info("Batch fetching usernames for {} ids", ids.size());

        Map<Long, String> idToNameMap = userService.getUsernamesByIds(ids);
        return ResponseEntity.ok(idToNameMap);
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
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        TokenResponse tokenResponse = userService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

}
