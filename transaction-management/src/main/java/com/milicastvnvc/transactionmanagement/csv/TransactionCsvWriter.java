package com.milicastvnvc.transactionmanagement.csv;

import com.milicastvnvc.transactionmanagement.config.TransactionProperties;
import com.milicastvnvc.transactionmanagement.exception.CsvWriteException;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.milicastvnvc.transactionmanagement.csv.TransactionCsvColumns.*;

@Component
@RequiredArgsConstructor
public class TransactionCsvWriter {

    private final TransactionProperties properties;

    @PostConstruct
    public void initializeFile() {
        Path path = resolvePath();

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.notExists(path) || Files.size(path) == 0) {
                writeHeader(path);
            }
        } catch (IOException e) {
            throw new CsvWriteException("Failed to initialize transaction CSV file.", e);
        }
    }

    public void append(TransactionModel transaction) {
        Path path = resolvePath();
        ensureLineSeparatorBeforeAppend(path);

        try (
                BufferedWriter writer = Files.newBufferedWriter(
                        Path.of(properties.filePath()),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.APPEND
                );

                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            printer.printRecord(
                    transaction.getTransactionDate(),
                    transaction.getAccountNumber(),
                    transaction.getAccountHolderName(),
                    transaction.getAmount().toPlainString(),
                    transaction.getStatus().getLabel()
            );
        } catch (IOException e) {
            throw new CsvWriteException("Failed to write new transaction in CSV file.", e);
        }
    }

    private Path resolvePath() {
        try {
            return Path.of(properties.filePath());
        } catch (InvalidPathException e) {
            throw new CsvWriteException("Configured transaction CSV path is invalid.", e);
        }
    }

    private void writeHeader(Path path) throws IOException {
        try (
                BufferedWriter writer = Files.newBufferedWriter(
                        path,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
                CSVPrinter printer = new CSVPrinter(
                        writer,
                        CSVFormat.DEFAULT.builder()
                                .setHeader(HEADERS)
                                .get()
                )
        ) {
            printer.flush();
        }
    }

    private void ensureLineSeparatorBeforeAppend(Path path) {
        try {
            if (Files.size(path) > 0 && !endsWithLineSeparator(path)) {
                Files.writeString(
                        path,
                        System.lineSeparator(),
                        StandardOpenOption.APPEND
                );
            }
        } catch (IOException e) {
            throw new CsvWriteException("Failed to access transaction CSV file.", e);
        }
    }

    private boolean endsWithLineSeparator(Path path) throws IOException {
        byte lastByte;

        try (var channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            channel.position(channel.size() - 1);

            ByteBuffer buffer = ByteBuffer.allocate(1);
            channel.read(buffer);
            buffer.flip();

            lastByte = buffer.get();
        }

        return lastByte == '\n' || lastByte == '\r';
    }
}
