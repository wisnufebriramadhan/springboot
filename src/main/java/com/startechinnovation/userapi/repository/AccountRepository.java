package com.startechinnovation.userapi.repository;

import com.startechinnovation.userapi.entity.Account;
import com.startechinnovation.userapi.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByBranch(Branch branch);
    long countByBranch(Branch branch);

    @Query("SELECT a.accountNumber FROM Account a WHERE a.branch = :branch")
    List<String> findAccountNumbersByBranch(@Param("branch") Branch branch);
}
