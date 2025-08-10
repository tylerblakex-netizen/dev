package com.example;

import com.example.model.Transaction;
import com.example.service.TransactionService;
import com.example.service.MetricsService;
import com.example.service.TransactionImportExportService;
import com.example.util.PerformanceUtils;
import com.example.util.ConcurrentUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Comprehensive test suite demonstrating Java 21 features
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class Java21FeaturesTest {
    
    private TransactionService transactionService;
    private MetricsService metricsService;
    private TransactionImportExportService importExportService;
    
    @BeforeEach
    void setUp() {
        metricsService = new MetricsService();
        transactionService = new TransactionService(metricsService);
        importExportService = new TransactionImportExportService(transactionService);
    }
    
    @Test
    @DisplayName("Test Records with Validation and Immutability")
    void testRecordFeatures() {
        // Test record creation and immutability
        var transaction = new Transaction(
                "TXN-001",
                new BigDecimal("150.00"),
                "USD",
                LocalDateTime.now(),
                "Test transaction",
                List.of("tag1", "tag2")
        );
        
        assertThat(transaction.id()).isEqualTo("TXN-001");
        assertThat(transaction.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(transaction.tags()).containsExactly("tag1", "tag2");
        
        // Test immutable collections
        assertThrows(UnsupportedOperationException.class, 
                    () -> transaction.tags().add("tag3"));
    }
    
    @Test
    @DisplayName("Test Pattern Matching with Switch Expressions")
    void testPatternMatching() {
        // Modern switch expressions with pattern matching
        var result = processValue("hello");
        assertThat(result).isEqualTo("String: HELLO");
        
        result = processValue(42);
        assertThat(result).isEqualTo("Integer: 42");
        
        result = processValue(List.of("a", "b", "c"));
        assertThat(result).isEqualTo("List with 3 elements");
    }
    
    private String processValue(Object value) {
        return switch (value) {
            case String s -> "String: " + s.toUpperCase();
            case Integer i -> "Integer: " + i;
            case List<?> list -> "List with " + list.size() + " elements";
            case null -> "Null value";
            default -> "Unknown type: " + value.getClass().getSimpleName();
        };
    }
    
    @Test
    @DisplayName("Test Text Blocks and String Templates")
    void testTextBlocks() {
        // Text blocks for multiline strings
        var json = """
                {
                    "transaction": {
                        "id": "TXN-001",
                        "amount": 150.00,
                        "currency": "USD"
                    }
                }
                """;
        
        assertThat(json).contains("TXN-001");
        assertThat(json).contains("150.00");
        
        // Enhanced string processing
        var lines = json.lines()
                .filter(line -> !line.trim().isEmpty())
                .filter(line -> line.contains(":"))
                .toList();
        
        assertThat(lines).hasSizeGreaterThan(3);
    }
    
    @Test
    @DisplayName("Test Virtual Threads and Async Processing")
    void testVirtualThreads() throws Exception {
        // Virtual threads for lightweight concurrency
        var results = Stream.of(1, 2, 3, 4, 5)
                .map(i -> CompletableFuture.supplyAsync(
                        () -> processWithDelay(i),
                        Thread.ofVirtual().factory()
                ))
                .map(CompletableFuture::join)
                .toList();
        
        assertThat(results).containsExactly(2, 4, 6, 8, 10);
    }
    
    private int processWithDelay(int value) {
        try {
            Thread.sleep(10); // Simulate some work
            return value * 2;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
    
    @Test
    @DisplayName("Test Concurrent Processing with Modern Patterns")
    void testConcurrentProcessing() {
        var items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // Test parallel processing
        var results = ConcurrentUtils.processInParallel(
                items,
                i -> i * i,
                4
        );
        
        assertThat(results).containsExactly(1, 4, 9, 16, 25, 36, 49, 64, 81, 100);
        
        // Test batch processing
        var batchResults = ConcurrentUtils.processByBatches(
                items,
                batch -> batch.stream().map(i -> i * 2).toList(),
                3
        );
        
        assertThat(batchResults).containsExactly(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
    }
    
    @Test
    @DisplayName("Test Performance Monitoring")
    void testPerformanceMonitoring() {
        // Test execution timing
        var result = PerformanceUtils.timeExecution(
                "fibonacci-calculation",
                () -> fibonacci(20)
        );
        
        assertThat(result).isEqualTo(6765);
        
        // Test async timing
        var asyncResult = PerformanceUtils.timeAsyncExecution(
                "async-calculation",
                () -> CompletableFuture.supplyAsync(() -> fibonacci(15))
        );
        
        assertThat(asyncResult.join()).isEqualTo(610);
    }
    
    private int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
    
    @Test
    @DisplayName("Test Service Layer Integration")
    void testServiceIntegration() {
        var transaction = new Transaction(
                "TXN-TEST-001",
                new BigDecimal("250.00"),
                "USD",
                LocalDateTime.now(),
                "Integration test transaction",
                List.of("test", "integration")
        );
        
        // Test service operations
        transactionService.addTransaction(transaction);
        
        var retrieved = transactionService.getTransaction("TXN-TEST-001");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().amount()).isEqualByComparingTo(new BigDecimal("250.00"));
        
        // Test batch operations
        var batch = List.of(
                new Transaction("BATCH-1", new BigDecimal("100"), "USD", 
                               LocalDateTime.now(), "Batch 1", List.of()),
                new Transaction("BATCH-2", new BigDecimal("200"), "EUR", 
                               LocalDateTime.now(), "Batch 2", List.of())
        );
        
        var batchFuture = transactionService.addTransactionsBatch(batch);
        assertThat(batchFuture.join()).hasSize(2);
    }
    
    @Test
    @DisplayName("Test Import/Export with JSON Processing")
    void testImportExport() throws Exception {
        var sampleData = importExportService.generateSampleTransactions(5);
        assertThat(sampleData).hasSize(5);
        
        // Test export
        var exportFuture = importExportService.exportTransactionsToJson(
                sampleData, "/tmp/test-export.json"
        );
        
        assertDoesNotThrow(() -> exportFuture.get());
        
        // Test import
        var importFuture = importExportService.importTransactionsFromJson("/tmp/test-export.json");
        var importedData = importFuture.get();
        
        assertThat(importedData).hasSize(5);
    }
    
    @Test
    @DisplayName("Test Exception Handling with Modern Patterns")
    void testExceptionHandling() {
        // Test resource management with try-with-resources
        assertDoesNotThrow(() -> {
            try (var timer = new PerformanceUtils.ExecutionTimer("test-operation")) {
                // Simulate some work
                Thread.sleep(1);
            }
        });
        
        // Test CompletableFuture exception handling
        var future = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test exception");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                return "Error handled: " + throwable.getMessage();
            }
            return result;
        });
        
        assertThat(future.join()).startsWith("Error handled:");
    }
    
    @Test
    @DisplayName("Test Stream API Advanced Patterns")
    void testAdvancedStreams() {
        var transactions = List.of(
                new Transaction("1", new BigDecimal("100"), "USD", 
                               LocalDateTime.now(), "T1", List.of("retail")),
                new Transaction("2", new BigDecimal("200"), "EUR", 
                               LocalDateTime.now(), "T2", List.of("wholesale")),
                new Transaction("3", new BigDecimal("150"), "USD", 
                               LocalDateTime.now(), "T3", List.of("retail"))
        );
        
        // Group by currency and sum amounts
        var currencySums = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::currency,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::amount,
                                BigDecimal::add
                        )
                ));
        
        assertThat(currencySums.get("USD")).isEqualByComparingTo(new BigDecimal("250"));
        assertThat(currencySums.get("EUR")).isEqualByComparingTo(new BigDecimal("200"));
        
        // Test parallel processing with streams
        var processedTransactions = transactions.parallelStream()
                .filter(t -> t.amount().compareTo(new BigDecimal("120")) > 0)
                .map(t -> t.withDescription(t.description() + " - PROCESSED"))
                .collect(Collectors.toList());
        
        assertThat(processedTransactions).hasSize(2);
        assertThat(processedTransactions.get(0).description()).endsWith("- PROCESSED");
    }
}
