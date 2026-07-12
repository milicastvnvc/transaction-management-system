package com.milicastvnvc.transactionmanagement.controller;

import com.milicastvnvc.transactionmanagement.dto.CreateTransactionRequest;
import com.milicastvnvc.transactionmanagement.dto.TransactionResponse;
import com.milicastvnvc.transactionmanagement.enums.TransactionStatus;
import com.milicastvnvc.transactionmanagement.config.CorsProperties;
import com.milicastvnvc.transactionmanagement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private static final String TRANSACTIONS_URL = "/transactions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        when(corsProperties.allowedOrigins())
                .thenReturn(List.of("http://localhost:4200"));
    }

    @Test
    void shouldReturnAllTransactions() throws Exception {
        TransactionResponse transaction = TransactionResponse.builder()
                .transactionDate(LocalDate.of(2025, 3, 1))
                .accountNumber("7289-3445-1121")
                .accountHolderName("Maria Johnson")
                .amount(new BigDecimal("150.00"))
                .status(TransactionStatus.SETTLED)
                .build();

        when(transactionService.getTransactions()).thenReturn(List.of(transaction));

        mockMvc.perform(get(TRANSACTIONS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].transactionDate")
                        .value("2025-03-01"))
                .andExpect(jsonPath("$[0].accountNumber")
                        .value("7289-3445-1121"))
                .andExpect(jsonPath("$[0].accountHolderName")
                        .value("Maria Johnson"))
                .andExpect(jsonPath("$[0].amount")
                        .value(150.00))
                .andExpect(jsonPath("$[0].status")
                        .value("SETTLED"));

        verify(transactionService).getTransactions();
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "John Smith",
                new BigDecimal("150.50")
        );

        TransactionResponse response = TransactionResponse.builder()
                .transactionDate(request.transactionDate())
                .accountNumber(request.accountNumber())
                .accountHolderName(request.accountHolderName())
                .amount(request.amount())
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionService.create(any(CreateTransactionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(TRANSACTIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionDate")
                        .value("2026-07-12"))
                .andExpect(jsonPath("$.accountNumber")
                        .value("1234-5678-9012"))
                .andExpect(jsonPath("$.accountHolderName")
                        .value("John Smith"))
                .andExpect(jsonPath("$.amount")
                        .value(150.50))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));

        verify(transactionService)
                .create(any(CreateTransactionRequest.class));
    }

    @Test
    void shouldReturnBadRequestWithValidationErrors()
            throws Exception {

        String invalidRequest = """
                {
                  "transactionDate": null,
                  "accountNumber": "",
                  "accountHolderName": "   ",
                  "amount": 150.555
                }
                """;

        mockMvc.perform(post(TRANSACTIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed."))
                .andExpect(jsonPath("$.fieldErrors.transactionDate")
                        .value("Transaction date is required"))
                .andExpect(jsonPath("$.fieldErrors.accountNumber")
                        .value("Account number is required"))
                .andExpect(jsonPath("$.fieldErrors.accountHolderName")
                        .value("Account holder name is required"))
                .andExpect(jsonPath("$.fieldErrors.amount")
                        .value(
                                "Maximum of 2 decimal places allowed"
                        ));

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyContainsInvalidDate()
            throws Exception {

        String invalidRequest = """
                {
                  "transactionDate": "not-a-date",
                  "accountNumber": "1234-5678-9012",
                  "accountHolderName": "John Smith",
                  "amount": 150.00
                }
                """;

        mockMvc.perform(post(TRANSACTIONS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Request body contains an invalid value or format"
                        ));

        verifyNoInteractions(transactionService);
    }
}
