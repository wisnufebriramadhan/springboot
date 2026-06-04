package com.startechinnovation.userapi.controller;

import com.startechinnovation.userapi.dto.ApiResponse;
import com.startechinnovation.userapi.dto.TransactionResponse;
import com.startechinnovation.userapi.dto.TransferRequest;
import com.startechinnovation.userapi.entity.Account;
import com.startechinnovation.userapi.entity.Transaction;
import com.startechinnovation.userapi.entity.User;
import com.startechinnovation.userapi.repository.UserRepository;
import com.startechinnovation.userapi.service.BankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banking")
@RequiredArgsConstructor
@Tag(name = "Banking Operations", description = "Operasi perbankan untuk nasabah")
@SecurityRequirement(name = "bearerAuth")
public class BankingController {

    private final BankingService bankingService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer Dana", description = "Melakukan transfer dana antar rekening")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Account source = bankingService.getAccountDetails(request.getSourceAccountNumber());
        if (!source.getOwner().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Access Denied: You can only transfer from your own account");
        }

        TransactionResponse response = bankingService.transfer(request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/account/inquiry/{accountNumber}")
    @Operation(summary = "Inquiry Rekening Tujuan", description = "Cek nama pemilik rekening tujuan")
    public ResponseEntity<ApiResponse<String>> inquiry(@PathVariable String accountNumber) {
        Account account = bankingService.getAccountDetails(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(account.getAccountHolderName()));
    }

    @GetMapping("/my-accounts")
    @Operation(summary = "Daftar Akun Saya", description = "Melihat semua rekening yang dimiliki oleh user yang sedang login")
    public ResponseEntity<ApiResponse<List<Account>>> getMyAccounts() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.success(bankingService.getAccountsByUsername(currentUsername)));
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Cek Detail Akun", description = "Melihat informasi akun & saldo")
    public ResponseEntity<ApiResponse<Account>> getAccount(@PathVariable String accountNumber) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = bankingService.getAccountDetailsSecure(accountNumber, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping("/account/{accountNumber}/history")
    @Operation(summary = "Riwayat Transaksi", description = "Melihat riwayat transaksi terakhir")
    public ResponseEntity<ApiResponse<List<Transaction>>> getHistory(@PathVariable String accountNumber) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Transaction> history = bankingService.getTransactionHistory(accountNumber, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
