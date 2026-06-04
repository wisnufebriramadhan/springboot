package com.startechinnovation.userapi.service;

import com.startechinnovation.userapi.dto.TransactionNotificationEvent;
import com.startechinnovation.userapi.dto.TransactionResponse;
import com.startechinnovation.userapi.dto.TransferRequest;
import com.startechinnovation.userapi.entity.Account;
import com.startechinnovation.userapi.entity.Transaction;
import com.startechinnovation.userapi.entity.TransactionSequence;
import com.startechinnovation.userapi.entity.User;
import com.startechinnovation.userapi.repository.AccountRepository;
import com.startechinnovation.userapi.repository.TransactionRepository;
import com.startechinnovation.userapi.repository.TransactionSequenceRepository;
import com.startechinnovation.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionSequenceRepository sequenceRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String currentUsername) {
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.getPin(), user.getPin())) {
            throw new RuntimeException("Invalid PIN");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct from source
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        
        // Add to destination
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber(request.getSourceAccountNumber()))
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .transactionDate(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        // Publish Asynchronous Event for Notification
        eventPublisher.publishEvent(new TransactionNotificationEvent(
                transaction.getReferenceNumber(),
                transaction.getSourceAccountNumber(),
                transaction.getDestinationAccountNumber(),
                transaction.getAmount(),
                transaction.getTransactionDate()
        ));

        return TransactionResponse.builder()
                .referenceNumber(transaction.getReferenceNumber())
                .status("SUCCESS")
                .message("Transfer completed successfully")
                .timestamp(transaction.getTransactionDate())
                .build();
    }

    private synchronized String generateReferenceNumber(String accountNumber) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqKey = accountNumber + "-" + dateStr;
        
        TransactionSequence seq = sequenceRepository.findById(seqKey)
                .orElse(TransactionSequence.builder().date(seqKey).lastSequence(0L).build());
        
        seq.setLastSequence(seq.getLastSequence() + 1);
        sequenceRepository.save(seq);
        
        return String.format("TRX-%s-%s%04d", accountNumber, dateStr, seq.getLastSequence());
    }

    public Account getAccountDetails(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public Account getAccountDetailsSecure(String accountNumber, String currentUsername) {
        Account account = getAccountDetails(accountNumber);
        if (!account.getOwner().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Access Denied: You are not the owner of this account");
        }
        return account;
    }

    public List<Account> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAccounts();
    }

    public List<Transaction> getTransactionHistory(String accountNumber, String currentUsername) {
        // Ensure account exists and belongs to user
        getAccountDetailsSecure(accountNumber, currentUsername);
        
        return transactionRepository.findBySourceAccountNumberOrDestinationAccountNumberOrderByTransactionDateDesc(
                accountNumber, accountNumber);
    }
}
