package com.startechinnovation.userapi.controller;

import com.startechinnovation.userapi.dto.ApiResponse;
import com.startechinnovation.userapi.dto.BranchRequest;
import com.startechinnovation.userapi.dto.RegisterAdminRequest;
import com.startechinnovation.userapi.entity.Account;
import com.startechinnovation.userapi.entity.AuditLog;
import com.startechinnovation.userapi.entity.Branch;
import com.startechinnovation.userapi.entity.Transaction;
import com.startechinnovation.userapi.entity.User;
import com.startechinnovation.userapi.repository.AccountRepository;
import com.startechinnovation.userapi.repository.AuditLogRepository;
import com.startechinnovation.userapi.repository.BranchRepository;
import com.startechinnovation.userapi.repository.TransactionRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Operasi khusus admin/super admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final BranchRepository branchRepository;
    private final BranchService branchService;
    private final UserService userService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_BRANCH_ADMIN')")
    @Operation(summary = "Dashboard Summary", description = "Melihat ringkasan statistik dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername).orElseThrow();

        Map<String, Object> summary = new HashMap<>();
        
        if (admin.getRole().equals("ROLE_SUPER_ADMIN")) {
            summary.put("totalAccounts", accountRepository.count());
            summary.put("totalBranches", branchRepository.count());
            summary.put("totalTransactions", transactionRepository.count());
            summary.put("totalAdmins", userRepository.findByRole("ROLE_BRANCH_ADMIN").size());
        } else {
            summary.put("totalAccounts", accountRepository.countByBranch(admin.getBranch()));
            summary.put("totalBranches", 1);
            summary.put("totalTransactions", transactionRepository.countAllByBranch(admin.getBranch()));
            summary.put("totalAdmins", 1);
        }

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

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

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_BRANCH_ADMIN')")
    @Operation(summary = "Daftar Transaksi", description = "Melihat semua transaksi (Super Admin) atau transaksi di cabangnya (Branch Admin)")
    public ResponseEntity<ApiResponse<List<com.startechinnovation.userapi.dto.TransactionDetailResponse>>> getAllTransactions() {
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername).orElseThrow();

        List<Transaction> transactions;
        if (admin.getRole().equals("ROLE_SUPER_ADMIN")) {
            transactions = transactionRepository.findAllByOrderByTransactionDateDesc();
        } else {
            transactions = transactionRepository.findAllByBranch(admin.getBranch());
        }

        List<com.startechinnovation.userapi.dto.TransactionDetailResponse> detailedTransactions = transactions.stream()
                .map(t -> {
                    String sourceName = accountRepository.findByAccountNumber(t.getSourceAccountNumber())
                            .map(Account::getAccountHolderName).orElse("Unknown");
                    String destName = accountRepository.findByAccountNumber(t.getDestinationAccountNumber())
                            .map(Account::getAccountHolderName).orElse("External/Unknown");

                    return com.startechinnovation.userapi.dto.TransactionDetailResponse.builder()
                            .id(t.getId())
                            .referenceNumber(t.getReferenceNumber())
                            .sourceAccountNumber(t.getSourceAccountNumber())
                            .sourceAccountName(sourceName)
                            .destinationAccountNumber(t.getDestinationAccountNumber())
                            .destinationAccountName(destName)
                            .amount(t.getAmount())
                            .status(t.getStatus())
                            .description(t.getDescription())
                            .transactionDate(t.getTransactionDate())
                            .build();
                }).toList();

        return ResponseEntity.ok(ApiResponse.success(detailedTransactions));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Audit Trail", description = "Melihat log aktivitas sistem (Super Admin only)")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success(auditLogRepository.findAllByOrderByTimestampDesc()));
    }

    @GetMapping("/branch-admins")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Daftar Admin Cabang", description = "Melihat semua user dengan role ROLE_BRANCH_ADMIN")
    public ResponseEntity<ApiResponse<List<User>>> getBranchAdmins() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findByRole("ROLE_BRANCH_ADMIN")));
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
