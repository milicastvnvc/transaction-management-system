package com.milicastvnvc.transactionmanagement.csv;

import com.milicastvnvc.transactionmanagement.config.TransactionProperties;
import com.milicastvnvc.transactionmanagement.exception.CsvReadException;
import com.milicastvnvc.transactionmanagement.mapper.TransactionCsvMapper;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static com.milicastvnvc.transactionmanagement.enums.TransactionStatus.PENDING;
import static com.milicastvnvc.transactionmanagement.enums.TransactionStatus.SETTLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionCsvReaderTest {

    @TempDir
    Path tempDirectory;

    private TransactionProperties properties;
    private TransactionCsvMapper mapper;
    private TransactionCsvReader csvReader;

    @BeforeEach
    void setUp() {
        properties = mock(TransactionProperties.class);
        mapper = mock(TransactionCsvMapper.class);

        csvReader = new TransactionCsvReader(properties, mapper);
    }

    @Test
    void shouldReadAndMapTransactionsFromCsvFile() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");

        Files.writeString(
                csvFile,
                """
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
                2025-03-02,1122-3456-7890,John Smith,75.50,Pending
                """,
                StandardCharsets.UTF_8
        );

        TransactionModel firstTransaction = TransactionModel.builder()
                .transactionDate(LocalDate.of(2025, 3, 1))
                .accountNumber("7289-3445-1121")
                .accountHolderName("Maria Johnson")
                .amount(new BigDecimal("150.00"))
                .status(SETTLED)
                .build();

        TransactionModel secondTransaction = TransactionModel.builder()
                .transactionDate(LocalDate.of(2025, 3, 2))
                .accountNumber("1122-3456-7890")
                .accountHolderName("John Smith")
                .amount(new BigDecimal("75.50"))
                .status(PENDING)
                .build();

        when(properties.filePath()).thenReturn(csvFile.toString());
        when(mapper.toTransaction(any(CSVRecord.class))).thenReturn(firstTransaction, secondTransaction);

        List<TransactionModel> result = csvReader.read();

        assertThat(result).containsExactly(firstTransaction, secondTransaction);

        ArgumentCaptor<CSVRecord> recordCaptor = ArgumentCaptor.forClass(CSVRecord.class);

        verify(mapper, times(2)).toTransaction(recordCaptor.capture());

        List<CSVRecord> records = recordCaptor.getAllValues();

        assertThat(records.getFirst().get("Transaction Date")).isEqualTo("2025-03-01");
        assertThat(records.getFirst().get("Account Holder Name")).isEqualTo("Maria Johnson");
        assertThat(records.get(0).get("Amount")).isEqualTo("150.00");
        assertThat(records.get(0).get("Status")).isEqualTo("Settled");

        assertThat(records.get(1).get("Transaction Date")).isEqualTo("2025-03-02");
        assertThat(records.get(1).get("Status")).isEqualTo("Pending");
    }

    @Test
    void shouldReturnEmptyListWhenCsvContainsOnlyHeader() throws IOException {
        Path csvFile = tempDirectory.resolve("empty-transactions.csv");

        Files.writeString(
                csvFile,
                """
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                """,
                StandardCharsets.UTF_8
        );

        when(properties.filePath()).thenReturn(csvFile.toString());

        List<TransactionModel> result = csvReader.read();

        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldTrimCsvValues() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions-with-spaces.csv");

        Files.writeString(
                csvFile,
                """
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                 2025-03-01 , 7289-3445-1121 , Maria Johnson , 150.00 , Settled
                """,
                StandardCharsets.UTF_8
        );

        TransactionModel transaction = mock(TransactionModel.class);

        when(properties.filePath()).thenReturn(csvFile.toString());
        when(mapper.toTransaction(any(CSVRecord.class)))
                .thenReturn(transaction);

        csvReader.read();

        ArgumentCaptor<CSVRecord> recordCaptor =
                ArgumentCaptor.forClass(CSVRecord.class);

        verify(mapper).toTransaction(recordCaptor.capture());

        CSVRecord record = recordCaptor.getValue();

        assertThat(record.get("Transaction Date")).isEqualTo("2025-03-01");
        assertThat(record.get("Account Number")).isEqualTo("7289-3445-1121");
        assertThat(record.get("Account Holder Name")).isEqualTo("Maria Johnson");
        assertThat(record.get("Amount")).isEqualTo("150.00");
        assertThat(record.get("Status")).isEqualTo("Settled");
    }

    @Test
    void shouldIgnoreHeaderCase() throws IOException {
        Path csvFile = tempDirectory.resolve("lowercase-headers.csv");

        Files.writeString(
                csvFile,
                """
                transaction date,account number,account holder name,amount,status
                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
                """,
                StandardCharsets.UTF_8
        );

        TransactionModel transaction = mock(TransactionModel.class);

        when(properties.filePath()).thenReturn(csvFile.toString());
        when(mapper.toTransaction(any(CSVRecord.class)))
                .thenReturn(transaction);

        csvReader.read();

        ArgumentCaptor<CSVRecord> recordCaptor =
                ArgumentCaptor.forClass(CSVRecord.class);

        verify(mapper).toTransaction(recordCaptor.capture());

        CSVRecord record = recordCaptor.getValue();

        assertThat(record.get("Transaction Date")).isEqualTo("2025-03-01");
        assertThat(record.get("Account Number")).isEqualTo("7289-3445-1121");
    }

    @Test
    void shouldThrowCsvReadExceptionWhenFileDoesNotExist() {
        Path missingFile = tempDirectory.resolve("missing-transactions.csv");

        when(properties.filePath()).thenReturn(missingFile.toString());

        assertThatThrownBy(csvReader::read)
                .isInstanceOf(CsvReadException.class)
                .hasMessage("Failed to read transaction CSV file.")
                .hasCauseInstanceOf(IOException.class);

        verifyNoInteractions(mapper);
    }

    @Test
    void shouldThrowCsvReadExceptionWhenConfiguredPathIsInvalid() {
        when(properties.filePath()).thenReturn("\0invalid-path");

        assertThatThrownBy(csvReader::read)
                .isInstanceOf(CsvReadException.class)
                .hasMessage("Configured transaction CSV path is invalid.")
                .hasCauseInstanceOf(InvalidPathException.class);

        verifyNoInteractions(mapper);
    }

    @Test
    void shouldPropagateMapperExceptionForInvalidCsvRecord() throws IOException {
        Path csvFile = tempDirectory.resolve("invalid-record.csv");

        Files.writeString(
                csvFile,
                """
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Unknown
                """,
                StandardCharsets.UTF_8
        );

        RuntimeException mappingException = new IllegalArgumentException("Unknown transaction status: Unknown");

        when(properties.filePath()).thenReturn(csvFile.toString());
        when(mapper.toTransaction(any(CSVRecord.class))).thenThrow(mappingException);

        assertThatThrownBy(csvReader::read).isSameAs(mappingException);
        verify(mapper).toTransaction(any(CSVRecord.class));
    }
}