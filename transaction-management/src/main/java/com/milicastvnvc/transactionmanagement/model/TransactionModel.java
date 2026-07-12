package com.milicastvnvc.transactionmanagement.model;

import com.milicastvnvc.transactionmanagement.enums.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class TransactionModel {
    private LocalDate transactionDate;
    private String accountNumber;
    private String accountHolderName;
    private BigDecimal amount;
    private TransactionStatus status;
}
