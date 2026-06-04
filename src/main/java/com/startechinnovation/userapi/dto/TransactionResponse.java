package com.startechinnovation.userapi.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String referenceNumber;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
