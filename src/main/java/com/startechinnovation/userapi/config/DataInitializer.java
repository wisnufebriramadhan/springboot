package com.startechinnovation.userapi.config;

import com.startechinnovation.userapi.entity.Account;
import com.startechinnovation.userapi.entity.Branch;
import com.startechinnovation.userapi.entity.User;
import com.startechinnovation.userapi.repository.AccountRepository;
import com.startechinnovation.userapi.repository.BranchRepository;
import com.startechinnovation.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() > 0) return;

            // Create Branches
            Branch branchA = Branch.builder().name("Jakarta").branchCode("JKT01").build();
            Branch branchB = Branch.builder().name("Surabaya").branchCode("SUB01").build();
            branchRepository.save(branchA);
            branchRepository.save(branchB);

            // Create Super Admin
            User superAdmin = User.builder()
                    .username("super_admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_SUPER_ADMIN")
                    .branch(branchA)
                    .build();
            userRepository.save(superAdmin);

            // Create Branch Admin
            User branchAdmin = User.builder()
                    .username("branch_admin_jkt")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_BRANCH_ADMIN")
                    .branch(branchA)
                    .build();
            userRepository.save(branchAdmin);

            // Create Users
            User wisnu = User.builder()
                    .username("wisnu")
                    .password(passwordEncoder.encode("password123"))
                    .role("ROLE_CUSTOMER")
                    .branch(branchA)
                    .build();
            userRepository.save(wisnu);

            // Create Accounts
            accountRepository.save(Account.builder()
                    .accountNumber("1234567890")
                    .accountHolderName("John Doe")
                    .balance(new BigDecimal("1000000.00"))
                    .createdAt(LocalDateTime.now())
                    .owner(wisnu)
                    .branch(branchA)
                    .build());
        };
    }
}

