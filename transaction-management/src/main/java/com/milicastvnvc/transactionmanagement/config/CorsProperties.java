package com.milicastvnvc.transactionmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "web.cors")
public record CorsProperties(List<String> allowedOrigins) {

}
