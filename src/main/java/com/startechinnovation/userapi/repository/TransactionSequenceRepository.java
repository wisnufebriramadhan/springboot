package com.startechinnovation.userapi.repository;

import com.startechinnovation.userapi.entity.TransactionSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionSequenceRepository extends JpaRepository<TransactionSequence, String> {
}
