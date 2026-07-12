package com.milicastvnvc.transactionmanagement.mapper;

import com.milicastvnvc.transactionmanagement.dto.TransactionResponse;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionResponseMapper {

    public TransactionResponse toResponse(TransactionModel transaction) {
        return TransactionResponse.builder()
                .transactionDate(transaction.getTransactionDate())
                .accountNumber(transaction.getAccountNumber())
                .accountHolderName(transaction.getAccountHolderName())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .build();
    }

    public List<TransactionResponse> toResponse(List<TransactionModel> transactions) {
        return transactions.stream()
                .map(this::toResponse)
                .toList();
    }
}
