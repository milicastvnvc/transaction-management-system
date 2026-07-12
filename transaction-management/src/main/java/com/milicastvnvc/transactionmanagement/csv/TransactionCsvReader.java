package com.milicastvnvc.transactionmanagement.csv;

import com.milicastvnvc.transactionmanagement.config.TransactionProperties;
import com.milicastvnvc.transactionmanagement.exception.CsvReadException;
import com.milicastvnvc.transactionmanagement.mapper.TransactionCsvMapper;
import com.milicastvnvc.transactionmanagement.model.TransactionModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionCsvReader {

    private final TransactionProperties properties;
    private final TransactionCsvMapper mapper;

    public List<TransactionModel> read() {
        Path path = resolvePath();

        try (
                Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                CSVParser parser = csvFormat().parse(reader);
        ) {
            return parser.stream()
                    .map(mapper::toTransaction)
                    .toList();
        } catch (IOException e) {
            throw new CsvReadException("Failed to read transaction CSV file.", e);
        }
    }

    private Path resolvePath() {
        try {
            return Path.of(properties.filePath());
        } catch (InvalidPathException e) {
            throw new CsvReadException("Configured transaction CSV path is invalid.", e);
        }
    }

    private CSVFormat csvFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .get();
    }
}
