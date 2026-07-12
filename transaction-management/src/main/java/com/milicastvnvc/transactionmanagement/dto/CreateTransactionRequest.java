package com.milicastvnvc.transactionmanagement.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(@NotNull(message = "Transaction date is required")
                                       LocalDate transactionDate,
                                       @NotBlank(message = "Account number is required")
                                       String accountNumber,
                                       @NotBlank(message = "Account holder name is required")
                                       String accountHolderName,
                                       @NotNull(message = "Amount is required")
                                       @Positive(message = "Amount must be greater than zero")
                                       @Digits(integer = 15, fraction = 2,  message = "Maximum of 2 decimal places allowed")
                                       BigDecimal amount) {
}
