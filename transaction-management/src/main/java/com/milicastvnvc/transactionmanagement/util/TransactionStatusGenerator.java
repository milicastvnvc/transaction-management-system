package com.milicastvnvc.transactionmanagement.util;

import com.milicastvnvc.transactionmanagement.enums.TransactionStatus;

import java.util.concurrent.ThreadLocalRandom;

public final class TransactionStatusGenerator {

    private TransactionStatusGenerator() {

    }

    public static TransactionStatus generate() {
        TransactionStatus[] enumArray = TransactionStatus.values();
        int indexOfRandomStatus = ThreadLocalRandom.current().nextInt(enumArray.length);

        return enumArray[indexOfRandomStatus];
    }
}
