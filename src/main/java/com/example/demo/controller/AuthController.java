package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private  AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.CREATED.value())
                .message("Đăng ký tài khoản thành công!")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authData = authService.login(request);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công!")
                .data(authData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestParam String refreshToken,
            @RequestHeader("Authorization") String oldAccessTokenHeader
    ) {
        AuthResponse authData = authService.refreshToken(refreshToken, oldAccessTokenHeader);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Làm mới token thành công!")
                .data(authData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ChangePasswordRequest request
    ) {
        String message = authService.changePassword(principal.getUser().getId(), request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(null)
                .error(null)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestParam String username,
            @RequestParam String newPassword
    ) {
        String result = authService.forgotPassword(username, newPassword);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message(result)
                .data(null)
                .error(null)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

}