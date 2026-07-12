package com.milicastvnvc.transactionmanagement.repository;

import com.milicastvnvc.transactionmanagement.model.TransactionModel;

import java.util.List;

public interface TransactionRepository {

    List<TransactionModel> getTransactions();

    TransactionModel create(TransactionModel transaction);
}
