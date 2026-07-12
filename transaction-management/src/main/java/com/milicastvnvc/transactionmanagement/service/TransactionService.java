package com.milicastvnvc.transactionmanagement.service;

import com.milicastvnvc.transactionmanagement.dto.CreateTransactionRequest;
import com.milicastvnvc.transactionmanagement.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {

    List<TransactionResponse> getTransactions();

    TransactionResponse create(CreateTransactionRequest request);
}
