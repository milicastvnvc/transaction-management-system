package com.milicastvnvc.transactionmanagement.service;

import com.milicastvnvc.transactionmanagement.dto.CreateTransactionRequest;
import com.milicastvnvc.transactionmanagement.dto.TransactionResponse;
import com.milicastvnvc.transactionmanagement.mapper.TransactionResponseMapper;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import com.milicastvnvc.transactionmanagement.repository.TransactionRepository;
import com.milicastvnvc.transactionmanagement.util.TransactionStatusGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionResponseMapper responseMapper;
    private final TransactionRepository transactionRepository;

    @Override
    public List<TransactionResponse> getTransactions() {
        return responseMapper.toResponse(transactionRepository.getTransactions());
    }

    @Override
    public TransactionResponse create(CreateTransactionRequest request) {
        TransactionModel transactionModel = TransactionModel.builder()
                .transactionDate(request.transactionDate())
                .accountNumber(request.accountNumber().trim())
                .accountHolderName(request.accountHolderName())
                .amount(normalizeAmount(request.amount()))
                .status(TransactionStatusGenerator.generate())
                .build();

        TransactionModel savedTransaction = transactionRepository.create(transactionModel);

        return responseMapper.toResponse(savedTransaction);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
