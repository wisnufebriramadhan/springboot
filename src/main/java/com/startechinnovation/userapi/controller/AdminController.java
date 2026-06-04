package com.startechinnovation.userapi.controller;

import com.startechinnovation.userapi.dto.ApiResponse;
import com.startechinnovation.userapi.dto.BranchRequest;
import com.startechinnovation.userapi.dto.RegisterAdminRequest;
import com.startechinnovation.userapi.entity.Account;
import com.startechinnovation.userapi.entity.Branch;
import com.startechinnovation.userapi.entity.User;
import com.startechinnovation.userapi.repository.AccountRepository;
import com.startechinnovation.userapi.repository.UserRepository;
import com.startechinnovation.userapi.service.BranchService;
import com.startechinnovation.userapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Operasi khusus admin/super admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BranchService branchService;
    private final UserService userService;

    @GetMapping("/accounts")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_BRANCH_ADMIN')")
    @Operation(summary = "Daftar Akun Cabang", description = "Melihat semua akun di cabang admin")
    public ResponseEntity<ApiResponse<List<Account>>> getAllAccountsInBranch() {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername).orElseThrow();

        List<Account> accounts;
        if (admin.getRole().equals("ROLE_SUPER_ADMIN")) {
            accounts = accountRepository.findAll();
        } else {
            accounts = accountRepository.findByBranch(admin.getBranch());
        }
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping("/register-admin")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Daftarkan Admin", description = "Membuat akun admin baru (Super Admin only)")
    public ResponseEntity<ApiResponse<String>> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        userService.registerAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Admin registered successfully"));
    }

    @PostMapping("/branches")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Buat Cabang", description = "Membuat cabang baru (Super Admin only)")
    public ResponseEntity<ApiResponse<Branch>> createBranch(@Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.createBranch(request)));
    }

    @GetMapping("/branches")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_BRANCH_ADMIN')")
    @Operation(summary = "Daftar Cabang", description = "Melihat daftar semua cabang")
    public ResponseEntity<ApiResponse<List<Branch>>> getAllBranches() {
        return ResponseEntity.ok(ApiResponse.success(branchService.getAllBranches()));
    }
}
