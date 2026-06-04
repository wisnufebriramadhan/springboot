package com.startechinnovation.userapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_sequences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSequence {
    @Id
    private String date; // YYYYMMDD
    private Long lastSequence;
}
