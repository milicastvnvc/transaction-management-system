package com.milicastvnvc.transactionmanagement.csv;

import com.milicastvnvc.transactionmanagement.config.TransactionProperties;
import com.milicastvnvc.transactionmanagement.enums.TransactionStatus;
import com.milicastvnvc.transactionmanagement.exception.CsvWriteException;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionCsvWriterTest {

    private static final String EXPECTED_HEADER = "Transaction Date,Account Number,Account Holder Name,Amount,Status";

    @TempDir
    Path tempDirectory;

    private TransactionProperties properties;
    private TransactionCsvWriter csvWriter;

    @BeforeEach
    void setUp() {
        properties = mock(TransactionProperties.class);
        csvWriter = new TransactionCsvWriter(properties);
    }

    @Test
    void shouldCreateParentDirectoriesFileAndHeaderWhenFileDoesNotExist() throws IOException {
        Path csvFile = tempDirectory.resolve("data").resolve("transactions.csv");
        when(properties.filePath()).thenReturn(csvFile.toString());

        csvWriter.initializeFile();

        assertThat(csvFile).exists().isRegularFile();
        List<String> lines = Files.readAllLines(
                csvFile,
                StandardCharsets.UTF_8
        );
        assertThat(lines).containsExactly(EXPECTED_HEADER);
    }

    @Test
    void shouldWriteHeaderWhenFileIsEmpty() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");
        Files.createFile(csvFile);

        when(properties.filePath()).thenReturn(csvFile.toString());

        csvWriter.initializeFile();

        List<String> lines = Files.readAllLines(
                csvFile,
                StandardCharsets.UTF_8
        );
        assertThat(lines).containsExactly(EXPECTED_HEADER);
    }

    @Test
    void shouldNotOverwriteExistingNonEmptyFile() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");

        String existingContent = """
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
                """;

        Files.writeString(
                csvFile,
                existingContent,
                StandardCharsets.UTF_8
        );

        when(properties.filePath()).thenReturn(csvFile.toString());

        csvWriter.initializeFile();

        assertThat(Files.readString(csvFile, StandardCharsets.UTF_8))
                .isEqualTo(existingContent);
    }

    @Test
    void shouldAppendTransactionToCsvFile() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");

        when(properties.filePath()).thenReturn(csvFile.toString());

        csvWriter.initializeFile();

        TransactionModel transaction = transaction(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "John Smith",
                "150.50",
                TransactionStatus.SETTLED
        );

        csvWriter.append(transaction);

        List<CSVRecord> records = readRecords(csvFile);

        assertThat(records).hasSize(1);

        CSVRecord record = records.getFirst();

        assertThat(record.get("Transaction Date")).isEqualTo("2026-07-12");
        assertThat(record.get("Account Number")).isEqualTo("1234-5678-9012");
        assertThat(record.get("Account Holder Name")).isEqualTo("John Smith");
        assertThat(record.get("Amount")).isEqualTo("150.50");
        assertThat(record.get("Status")).isEqualTo("Settled");
    }

    @Test
    void shouldAddLineSeparatorBeforeAppendingWhenFileDoesNotEndWithOne() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");

        String initialContent = EXPECTED_HEADER + System.lineSeparator()
                + "2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled";

        Files.writeString(
                csvFile,
                initialContent,
                StandardCharsets.UTF_8
        );

        when(properties.filePath()).thenReturn(csvFile.toString());

        TransactionModel transaction = transaction(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "John Smith",
                "75.50",
                TransactionStatus.PENDING
        );

        csvWriter.append(transaction);

        List<CSVRecord> records = readRecords(csvFile);

        assertThat(records).hasSize(2);
        assertThat(records.get(0).get("Account Holder Name")).isEqualTo("Maria Johnson");
        assertThat(records.get(1).get("Account Holder Name")).isEqualTo("John Smith");
    }

    @Test
    void shouldPreserveAmountWithTwoDecimalPlaces() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");

        when(properties.filePath()).thenReturn(csvFile.toString());

        csvWriter.initializeFile();

        TransactionModel transaction = transaction(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "John Smith",
                "150.00",
                TransactionStatus.SETTLED
        );

        csvWriter.append(transaction);

        CSVRecord record = readRecords(csvFile).getFirst();

        assertThat(record.get("Amount")).isEqualTo("150.00");
    }

    @Test
    void shouldWriteSpecialCharactersUsingUtf8() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");

        when(properties.filePath()).thenReturn(csvFile.toString());

        csvWriter.initializeFile();

        TransactionModel transaction = transaction(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "Milića Šćepanović",
                "100.00",
                TransactionStatus.SETTLED
        );

        csvWriter.append(transaction);

        CSVRecord record = readRecords(csvFile).getFirst();

        assertThat(record.get("Account Holder Name")).isEqualTo("Milića Šćepanović");
    }

    @Test
    void shouldEscapeCsvSpecialCharacters() throws IOException {
        Path csvFile = tempDirectory.resolve("transactions.csv");

        when(properties.filePath()).thenReturn(csvFile.toString());

        csvWriter.initializeFile();

        TransactionModel transaction = transaction(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "Smith, John",
                "100.00",
                TransactionStatus.SETTLED
        );

        csvWriter.append(transaction);

        CSVRecord record = readRecords(csvFile).getFirst();

        assertThat(record.get("Account Holder Name")).isEqualTo("Smith, John");
    }

    @Test
    void shouldThrowCsvWriteExceptionWhenConfiguredPathIsInvalid() {
        when(properties.filePath()).thenReturn("\0invalid-path");

        assertThatThrownBy(csvWriter::initializeFile)
                .isInstanceOf(CsvWriteException.class)
                .hasMessage("Configured transaction CSV path is invalid.")
                .hasCauseInstanceOf(InvalidPathException.class);
    }

    @Test
    void shouldThrowCsvWriteExceptionWhenAppendFileDoesNotExist() {
        Path missingFile = tempDirectory.resolve("missing.csv");

        when(properties.filePath()).thenReturn(missingFile.toString());

        TransactionModel transaction = transaction(
                LocalDate.of(2026, 7, 12),
                "1234-5678-9012",
                "John Smith",
                "100.00",
                TransactionStatus.SETTLED
        );

        assertThatThrownBy(() -> csvWriter.append(transaction))
                .isInstanceOf(CsvWriteException.class)
                .hasMessage("Failed to access transaction CSV file.")
                .hasCauseInstanceOf(IOException.class);
    }

    private TransactionModel transaction(
            LocalDate date,
            String accountNumber,
            String accountHolderName,
            String amount,
            TransactionStatus status
    ) {
        return TransactionModel.builder()
                .transactionDate(date)
                .accountNumber(accountNumber)
                .accountHolderName(accountHolderName)
                .amount(new BigDecimal(amount))
                .status(status)
                .build();
    }

    private List<CSVRecord> readRecords(Path path) throws IOException {
        try (
                var reader = Files.newBufferedReader(
                        path,
                        StandardCharsets.UTF_8
                );
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader)
        ) {
            return parser.getRecords();
        }
    }
}