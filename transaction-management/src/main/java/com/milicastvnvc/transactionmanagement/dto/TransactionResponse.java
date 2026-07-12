package com.milicastvnvc.transactionmanagement.dto;

import com.milicastvnvc.transactionmanagement.enums.TransactionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record TransactionResponse(LocalDate transactionDate,
                                  String accountNumber,
                                  String accountHolderName,
                                  BigDecimal amount,
                                  TransactionStatus status) {
}