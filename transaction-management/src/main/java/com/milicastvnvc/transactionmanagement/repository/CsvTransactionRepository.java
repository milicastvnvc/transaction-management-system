package com.milicastvnvc.transactionmanagement.repository;

import com.milicastvnvc.transactionmanagement.csv.TransactionCsvReader;
import com.milicastvnvc.transactionmanagement.csv.TransactionCsvWriter;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
@RequiredArgsConstructor
public class CsvTransactionRepository implements TransactionRepository {

    private final TransactionCsvReader csvReader;
    private final TransactionCsvWriter csvWriter;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public List<TransactionModel> getTransactions() {
        lock.readLock().lock();

        try {
            return csvReader.read();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public TransactionModel create(TransactionModel transaction) {
        lock.writeLock().lock();

        try {
            csvWriter.append(transaction);

            return transaction;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
