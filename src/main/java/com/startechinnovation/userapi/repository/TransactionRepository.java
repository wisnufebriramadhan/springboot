package com.startechinnovation.userapi.repository;

import com.startechinnovation.userapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountNumberOrDestinationAccountNumberOrderByTransactionDateDesc(
            String sourceAccountNumber, String destinationAccountNumber);
}
