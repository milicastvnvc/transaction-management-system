package com.milicastvnvc.transactionmanagement.csv;

public final class TransactionCsvColumns {

    private TransactionCsvColumns() {
    }

    public static final String TRANSACTION_DATE = "Transaction Date";
    public static final String ACCOUNT_NUMBER = "Account Number";
    public static final String ACCOUNT_HOLDER_NAME = "Account Holder Name";
    public static final String AMOUNT = "Amount";
    public static final String STATUS = "Status";

    public static final String[] HEADERS = {
            TRANSACTION_DATE,
            ACCOUNT_NUMBER,
            ACCOUNT_HOLDER_NAME,
            AMOUNT,
            STATUS
    };
}
