package com.milicastvnvc.transactionmanagement.service;

import com.milicastvnvc.transactionmanagement.dto.CreateTransactionRequest;
import com.milicastvnvc.transactionmanagement.dto.TransactionResponse;
import com.milicastvnvc.transactionmanagement.enums.TransactionStatus;
import com.milicastvnvc.transactionmanagement.mapper.TransactionResponseMapper;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import com.milicastvnvc.transactionmanagement.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private static final String EXPECTED_ACCOUNT_NUMBER = "1234-5678-9012";
    private static final String EXPECTED_ACCOUNT_HOLDER_NAME = "John Smith";
    private static final String EXPECTED_AMOUNT = "150.50";

    @Mock
    private TransactionResponseMapper responseMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldReturnMappedTransactions() {
        TransactionModel transaction = TransactionModel.builder()
                .transactionDate(LocalDate.of(2025, 3, 1))
                .accountNumber("7289-3445-1121")
                .accountHolderName("Maria Johnson")
                .amount(new BigDecimal("150.00"))
                .status(TransactionStatus.SETTLED)
                .build();
        TransactionResponse response = TransactionResponse.builder()
                .transactionDate(transaction.getTransactionDate())
                .accountNumber(transaction.getAccountNumber())
                .accountHolderName(transaction.getAccountHolderName())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .build();
        List<TransactionModel> transactions = List.of(transaction);
        List<TransactionResponse> expectedResponse = List.of(response);
        when(transactionRepository.getTransactions()).thenReturn(transactions);
        when(responseMapper.toResponse(transactions)).thenReturn(expectedResponse);

        List<TransactionResponse> result =
                transactionService.getTransactions();

        assertEquals(expectedResponse, result);
        verify(transactionRepository).getTransactions();
        verify(responseMapper).toResponse(transactions);
        verifyNoMoreInteractions(transactionRepository, responseMapper);
    }

    @Test
    void shouldCreateTransactionAndReturnMappedResponse() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                LocalDate.of(2026, 7, 12),
                " 1234-5678-9012 ",
                "John Smith",
                new BigDecimal("150.5")
        );
        TransactionResponse expectedResponse = TransactionResponse.builder()
                .transactionDate(request.transactionDate())
                .accountNumber(EXPECTED_ACCOUNT_NUMBER)
                .accountHolderName(EXPECTED_ACCOUNT_HOLDER_NAME)
                .amount(new BigDecimal(EXPECTED_AMOUNT))
                .status(TransactionStatus.PENDING)
                .build();
        when(transactionRepository.create(any(TransactionModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(responseMapper.toResponse(any(TransactionModel.class))).thenReturn(expectedResponse);

        TransactionResponse result = transactionService.create(request);

        assertEquals(expectedResponse, result);
        ArgumentCaptor<TransactionModel> transactionCaptor = ArgumentCaptor.forClass(TransactionModel.class);
        verify(transactionRepository).create(transactionCaptor.capture());
        TransactionModel savedTransaction = transactionCaptor.getValue();

        assertEquals(request.transactionDate(), savedTransaction.getTransactionDate());
        assertEquals(EXPECTED_ACCOUNT_NUMBER, savedTransaction.getAccountNumber());
        assertEquals(EXPECTED_ACCOUNT_HOLDER_NAME, savedTransaction.getAccountHolderName());
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(EXPECTED_AMOUNT);
        assertThat(savedTransaction.getAmount().scale()).isEqualTo(2);
        assertTrue(Arrays.stream(TransactionStatus.values())
                .anyMatch(status -> status == savedTransaction.getStatus()));
        verify(responseMapper).toResponse(savedTransaction);
        verifyNoMoreInteractions(transactionRepository, responseMapper);
    }

    @Test
    void shouldNormalizeWholeAmountToTwoDecimalPlaces() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "John Smith",
                new BigDecimal("150")
        );
        when(transactionRepository.create(any(TransactionModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(responseMapper.toResponse(any(TransactionModel.class))).thenReturn(mock(TransactionResponse.class));

        transactionService.create(request);

        ArgumentCaptor<TransactionModel> transactionCaptor = ArgumentCaptor.forClass(TransactionModel.class);
        verify(transactionRepository).create(transactionCaptor.capture());
        BigDecimal savedAmount = transactionCaptor.getValue().getAmount();
        assertThat(savedAmount).isEqualByComparingTo("150.00");
        assertThat(savedAmount.scale()).isEqualTo(2);
    }

    @Test
    void shouldNotPersistTransactionWhenAmountRequiresRounding() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "John Smith",
                new BigDecimal("150.555")
        );

        assertThatThrownBy(() -> transactionService.create(request)).isInstanceOf(ArithmeticException.class);
        verifyNoInteractions(transactionRepository, responseMapper);
    }
}