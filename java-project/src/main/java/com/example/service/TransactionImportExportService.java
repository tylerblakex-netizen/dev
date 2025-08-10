package com.example.service;

import com.example.model.Transaction;
import com.example.model.TransactionMetadata;
import com.example.model.TransactionType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for importing/exporting transactions with JSON processing
 */
public class TransactionImportExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionImportExportService.class);
    
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    
    public TransactionImportExportService(TransactionService transactionService) {
        this.transactionService = transactionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Export transactions to JSON file asynchronously
     */
    public CompletableFuture<Path> exportToJson(List<Transaction> transactions, Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Exporting {} transactions to {}", transactions.size(), filePath);
                
                var json = objectMapper.writerWithDefaultPrettyPrinter()
                                      .writeValueAsString(transactions);
                
                Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                
                logger.info("Successfully exported transactions to {}", filePath);
                return filePath;
                
            } catch (IOException e) {
                logger.error("Failed to export transactions to {}", filePath, e);
                throw new RuntimeException("Export failed", e);
            }
        });
    }
    
    /**
     * Import transactions from JSON file asynchronously
     */
    public CompletableFuture<List<Transaction>> importFromJson(Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Importing transactions from {}", filePath);
                
                var json = Files.readString(filePath);
                var transactions = objectMapper.readValue(json, new TypeReference<List<Transaction>>() {});
                
                logger.info("Successfully imported {} transactions from {}", transactions.size(), filePath);
                return transactions;
                
            } catch (IOException e) {
                logger.error("Failed to import transactions from {}", filePath, e);
                throw new RuntimeException("Import failed", e);
            }
        });
    }
    
    /**
     * Import and save transactions in one operation
     */
    public CompletableFuture<List<Transaction>> importAndSave(Path filePath) {
        return importFromJson(filePath)
                .thenCompose(transactionService::saveBulkAsync);
    }
    
    /**
     * Create sample transactions for testing
     */
    public List<Transaction> createSampleTransactions() {
        return List.of(
            new Transaction(
                UUID.randomUUID().toString(),
                "Coffee purchase",
                new BigDecimal("4.50"),
                LocalDateTime.now().minusHours(2),
                TransactionType.DEBIT,
                List.of("food", "coffee"),
                TransactionMetadata.withSource("mobile-app")
            ),
            new Transaction(
                UUID.randomUUID().toString(),
                "Salary deposit",
                new BigDecimal("3500.00"),
                LocalDateTime.now().minusDays(1),
                TransactionType.CREDIT,
                List.of("salary", "income"),
                TransactionMetadata.empty()
            ),
            new Transaction(
                UUID.randomUUID().toString(),
                "Transfer to savings",
                new BigDecimal("500.00"),
                LocalDateTime.now().minusHours(6),
                TransactionType.TRANSFER,
                List.of("savings", "transfer"),
                TransactionMetadata.withSource("web-app")
            )
        );
    }
    
    /**
     * Export current transaction data
     */
    public CompletableFuture<Path> exportCurrentData(Path directory) {
        var fileName = "transactions_" + 
                      LocalDateTime.now().toString().replace(":", "-") + 
                      ".json";
        var filePath = directory.resolve(fileName);
        
        var recentTransactions = transactionService.getRecentTransactions();
        return exportToJson(recentTransactions, filePath);
    }
}
