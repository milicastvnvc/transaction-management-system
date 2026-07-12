package com.milicastvnvc.transactionmanagement.mapper;

import com.milicastvnvc.transactionmanagement.enums.TransactionStatus;
import com.milicastvnvc.transactionmanagement.exception.InvalidCsvRecordException;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.milicastvnvc.transactionmanagement.csv.TransactionCsvColumns.*;

@Component
public class TransactionCsvMapper {

    public TransactionModel toTransaction(CSVRecord record) {
        try {
            return TransactionModel.builder()
                    .transactionDate(LocalDate.parse(record.get(TRANSACTION_DATE)))
                    .accountNumber(record.get(ACCOUNT_NUMBER))
                    .accountHolderName(record.get(ACCOUNT_HOLDER_NAME))
                    .amount(new BigDecimal(record.get(AMOUNT)))
                    .status(TransactionStatus.from(record.get(STATUS)))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new InvalidCsvRecordException("Invalid CSV record at row: " + record.getRecordNumber(), e);
        }
    }
}
