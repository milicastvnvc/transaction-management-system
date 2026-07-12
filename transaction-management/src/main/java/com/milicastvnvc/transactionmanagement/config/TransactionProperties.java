package com.milicastvnvc.transactionmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "transaction")
public record TransactionProperties(String filePath) {}
