package com.startechinnovation.userapi.dto;

import com.startechinnovation.userapi.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {
    private Long id;
    private String referenceNumber;
    private String sourceAccountNumber;
    private String sourceAccountName;
    private String destinationAccountNumber;
    private String destinationAccountName;
    private BigDecimal amount;
    private Transaction.TransactionStatus status;
    private String description;
    private LocalDateTime transactionDate;
}
