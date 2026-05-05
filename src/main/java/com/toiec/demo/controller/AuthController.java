package com.toiec.demo.controller;

import com.toiec.demo.annotation.RateLimit;
import com.toiec.demo.dtos.request.GoogleLoginRequest;
import com.toiec.demo.dtos.request.LoginRequest;
import com.toiec.demo.dtos.request.RegisterRequest;
import com.toiec.demo.dtos.response.ApiResponse;
import com.toiec.demo.dtos.response.AuthResponse;
import com.toiec.demo.dtos.response.UserProfileResponse;
import com.toiec.demo.security.CurrentUser;
import com.toiec.demo.security.UserPrincipal;
import com.toiec.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @RateLimit(name = "register", capacity = 5, refillTokens = 1, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.register(request)));
    }

    @PostMapping("/login")
    @RateLimit(name = "login", capacity = 10, refillTokens = 2, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.login(request)));
    }

    @GetMapping("/me")
    @RateLimit(name = "me", capacity = 30, refillTokens = 15, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(currentUser.getId())));
    }

    @PostMapping("/google")
    @RateLimit(name = "google", capacity = 10, refillTokens = 2, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.loginWithGoogle(request.getIdToken())));
    }

    @PostMapping("/logout")
    @RateLimit(name = "logout", capacity = 20, refillTokens = 10, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7);
        userService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}