package com.startechinnovation.userapi.service;

import com.startechinnovation.userapi.dto.RegisterAdminRequest;
import com.startechinnovation.userapi.dto.RegisterRequest;
import com.startechinnovation.userapi.entity.Branch;
import com.startechinnovation.userapi.entity.Account;
import com.startechinnovation.userapi.entity.User;
import com.startechinnovation.userapi.repository.AccountRepository;
import com.startechinnovation.userapi.repository.BranchRepository;
import com.startechinnovation.userapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerAdmin(RegisterAdminRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Branch branch = branchRepository.findByBranchCode(request.getBranchCode())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        User admin = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .branch(branch)
                .build();
        userRepository.save(admin);
    }

    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Branch branch = branchRepository.findByBranchCode(request.getBranchCode())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // Create User
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_CUSTOMER")
                .branch(branch)
                .build();
        userRepository.save(user);

        // Create Account
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .owner(user)
                .branch(branch)
                .build();
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        return String.valueOf(1000000000L + new Random().nextInt(900000000));
    }
}
