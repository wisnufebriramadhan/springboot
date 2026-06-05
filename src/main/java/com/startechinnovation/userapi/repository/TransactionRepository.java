package com.startechinnovation.userapi.repository;

import com.startechinnovation.userapi.entity.Branch;
import com.startechinnovation.userapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountNumberOrDestinationAccountNumberOrderByTransactionDateDesc(
            String sourceAccountNumber, String destinationAccountNumber);

    @Query("SELECT t FROM Transaction t WHERE " +
           "t.sourceAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.branch = :branch) OR " +
           "t.destinationAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.branch = :branch) " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByBranch(@Param("branch") Branch branch);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
           "t.sourceAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.branch = :branch) OR " +
           "t.destinationAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.branch = :branch)")
    long countAllByBranch(@Param("branch") Branch branch);

    List<Transaction> findAllByOrderByTransactionDateDesc();
}
