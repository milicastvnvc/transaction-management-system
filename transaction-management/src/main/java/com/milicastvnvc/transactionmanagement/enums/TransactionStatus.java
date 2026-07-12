package com.milicastvnvc.transactionmanagement.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TransactionStatus {
    PENDING("Pending"),
    SETTLED("Settled"),
    FAILED("Failed");

    private final String label;

    TransactionStatus(String label) {
        this.label = label;
    }

    public static TransactionStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Transaction status must not be blank");
        }

        return Arrays.stream(values())
                .filter(status -> status.getLabel().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown transaction status: " + value));
    }
}
