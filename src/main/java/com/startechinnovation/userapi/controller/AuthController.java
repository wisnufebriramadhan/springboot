package com.startechinnovation.userapi.controller;

import jakarta.validation.Valid;
import com.startechinnovation.userapi.dto.SetPinRequest;
import com.startechinnovation.userapi.service.UserService;
import com.startechinnovation.userapi.dto.RegisterRequest;
import com.startechinnovation.userapi.dto.ApiResponse;
import com.startechinnovation.userapi.config.JwtService;
import com.startechinnovation.userapi.entity.User;
import com.startechinnovation.userapi.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints untuk login dan registrasi")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login untuk mendapatkan token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtService.generateToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(ApiResponse.success(new AuthResponse(token)));
        }
        return ResponseEntity.status(401).body(ApiResponse.error("01", "Invalid credentials"));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrasi Nasabah", description = "Registrasi user baru dan buat rekening otomatis")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully"));
    }

    @PostMapping("/set-pin")
    @Operation(summary = "Set PIN", description = "Mengatur PIN transaksi nasabah")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> setPin(@Valid @RequestBody SetPinRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.setPin(currentUsername, request.getPin());
        return ResponseEntity.ok(ApiResponse.success("PIN set successfully"));
    }

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
    }

    @Data
    @RequiredArgsConstructor
    public static class AuthResponse {
        private final String token;
    }
}
