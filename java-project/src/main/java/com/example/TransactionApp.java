package com.example;

import com.example.model.Transaction;
import com.example.service.TransactionService;
import com.example.service.MetricsService;
import com.example.service.TransactionImportExportService;
import com.example.util.PerformanceUtils;
import com.example.util.ConcurrentUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Enterprise Transaction Processing Application - Java 21 Showcase
 * 
 * This application demonstrates modern Java 21 features including:
 * - Records for immutable data models
 * - Pattern matching with switch expressions
 * - Text blocks for readable code
 * - Virtual threads for lightweight concurrency
 * - Enhanced Stream API with parallel processing
 * - Modern exception handling patterns
 * - Performance monitoring and metrics
 */
public class TransactionApp {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionApp.class);
    
    private final TransactionService transactionService;
    private final MetricsService metricsService;
    private final TransactionImportExportService importExportService;
    
    public TransactionApp() {
        this.metricsService = new MetricsService();
        this.transactionService = new TransactionService(metricsService);
        this.importExportService = new TransactionImportExportService(transactionService);
    }
    
    public static void main(String[] args) {
        logger.info("Starting Enterprise Transaction Processing Application");
        
        var app = new TransactionApp();
        
        try {
            app.demonstrateFeatures();
        } catch (Exception e) {
            logger.error("Application error", e);
            System.exit(1);
        }
        
        logger.info("Application completed successfully");
    }
    
    public void demonstrateFeatures() throws Exception {
        logger.info("=== Demonstrating Java 21 Enterprise Features ===");
        
        // 1. Modern Record-based Data Models
        demonstrateRecords();
        
        // 2. Pattern Matching and Switch Expressions
        demonstratePatternMatching();
        
        // 3. Virtual Threads and Concurrent Processing
        demonstrateVirtualThreads();
        
        // 4. Stream API Advanced Patterns
        demonstrateAdvancedStreams();
        
        // 5. Performance Monitoring
        demonstratePerformanceMonitoring();
        
        // 6. Async Processing with CompletableFuture
        demonstrateAsyncProcessing();
        
        // 7. JSON Import/Export Operations
        demonstrateJsonOperations();
        
        // 8. Enterprise Service Integration
        demonstrateServiceIntegration();
    }
    
    private void demonstrateRecords() {
        logger.info("--- Demonstrating Records and Immutable Data ---");
        
        var transaction = new Transaction(
                "TXN-DEMO-001",
                new BigDecimal("1500.00"),
                "USD",
                LocalDateTime.now(),
                "Enterprise transaction demonstration",
                List.of("demo", "enterprise", "java21")
        );
        
        logger.info("Created transaction: {} with amount: {} {}",
                   transaction.id(),
                   transaction.amount(),
                   transaction.currency());
        
        // Demonstrate immutability
        logger.info("Transaction tags are immutable: {}", transaction.tags());
        
        // Demonstrate record methods
        var copy = transaction.withDescription("Updated description");
        logger.info("Created copy with new description: {}", copy.description());
    }
    
    private void demonstratePatternMatching() {
        logger.info("--- Demonstrating Pattern Matching ---");
        
        var testValues = List.of(
                "Hello World",
                42,
                List.of("a", "b", "c"),
                null,
                new BigDecimal("100.50")
        );
        
        testValues.forEach(value -> {
            var result = processValueWithPatternMatching(value);
            logger.info("Value: {} -> Result: {}", value, result);
        });
    }
    
    private String processValueWithPatternMatching(Object value) {
        return switch (value) {
            case String s when s.length() > 5 -> 
                "Long string: " + s.substring(0, 5) + "...";
            case String s -> 
                "Short string: " + s.toUpperCase();
            case Integer i when i > 100 -> 
                "Large number: " + i;
            case Integer i -> 
                "Small number: " + i;
            case List<?> list when list.size() > 2 -> 
                "Large list with " + list.size() + " elements";
            case List<?> list -> 
                "Small list: " + list;
            case BigDecimal bd -> 
                "Decimal: " + bd.toPlainString();
            case null -> 
                "Null value encountered";
            default -> 
                "Unknown type: " + value.getClass().getSimpleName();
        };
    }
    
    private void demonstrateVirtualThreads() throws Exception {
        logger.info("--- Demonstrating Virtual Threads ---");
        
        var tasks = IntStream.range(1, 11)
                .mapToObj(i -> CompletableFuture.supplyAsync(
                        () -> simulateWork(i),
                        Thread.ofVirtual().factory()
                ))
                .toList();
        
        var results = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> tasks.stream()
                        .map(CompletableFuture::join)
                        .toList())
                .get();
        
        logger.info("Virtual thread results: {}", results);
    }
    
    private String simulateWork(int taskId) {
        try {
            Thread.sleep(50); // Simulate I/O work
            return "Task-" + taskId + " completed on " + Thread.currentThread();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Task-" + taskId + " interrupted";
        }
    }
    
    private void demonstrateAdvancedStreams() {
        logger.info("--- Demonstrating Advanced Stream Patterns ---");
        
        var numbers = IntStream.range(1, 21).boxed().toList();
        
        // Parallel processing with streams
        var processedNumbers = numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .sorted()
                .toList();
        
        logger.info("Processed even squares: {}", processedNumbers);
        
        // Custom parallel processing
        var concurrentResults = ConcurrentUtils.processInParallel(
                numbers,
                n -> fibonacci(n),
                Runtime.getRuntime().availableProcessors()
        );
        
        logger.info("Fibonacci results (first 10): {}", 
                   concurrentResults.subList(0, 10));
    }
    
    private long fibonacci(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
    
    private void demonstratePerformanceMonitoring() {
        logger.info("--- Demonstrating Performance Monitoring ---");
        
        // Time synchronous operation
        var result = PerformanceUtils.timeExecution(
                "large-computation",
                () -> IntStream.range(1, 1000000)
                        .parallel()
                        .mapToLong(Integer::longValue)
                        .sum()
        );
        
        logger.info("Large computation result: {}", result);
        
        // Time asynchronous operation
        var asyncResult = PerformanceUtils.timeAsyncExecution(
                "async-computation",
                () -> CompletableFuture.supplyAsync(() -> 
                        IntStream.range(1, 100000)
                                .reduce(0, Integer::sum))
        );
        
        logger.info("Async computation result: {}", asyncResult.join());
        
        // Using try-with-resources for timing
        try (var timer = new PerformanceUtils.ExecutionTimer("batch-operation")) {
            IntStream.range(1, 1000)
                    .parallel()
                    .forEach(i -> Math.sqrt(i));
        }
    }
    
    private void demonstrateAsyncProcessing() throws Exception {
        logger.info("--- Demonstrating Async Processing ---");
        
        var sampleTransactions = List.of(
                new Transaction("ASYNC-1", new BigDecimal("100"), "USD", 
                               LocalDateTime.now(), "Async 1", List.of()),
                new Transaction("ASYNC-2", new BigDecimal("200"), "EUR", 
                               LocalDateTime.now(), "Async 2", List.of()),
                new Transaction("ASYNC-3", new BigDecimal("300"), "GBP", 
                               LocalDateTime.now(), "Async 3", List.of())
        );
        
        var batchResult = transactionService.addTransactionsBatch(sampleTransactions);
        var addedTransactions = batchResult.get();
        
        logger.info("Added {} transactions asynchronously", addedTransactions.size());
        
        // Demonstrate timeout handling
        var timeoutFuture = PerformanceUtils.withTimeout(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100);
                        return "Completed within timeout";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "Interrupted";
                    }
                }),
                java.time.Duration.ofSeconds(1)
        );
        
        logger.info("Timeout result: {}", timeoutFuture.get());
    }
    
    private void demonstrateJsonOperations() throws Exception {
        logger.info("--- Demonstrating JSON Import/Export ---");
        
        var sampleData = importExportService.generateSampleTransactions(5);
        logger.info("Generated {} sample transactions", sampleData.size());
        
        var exportPath = "/tmp/demo-transactions.json";
        var exportResult = importExportService.exportTransactionsToJson(sampleData, exportPath);
        exportResult.get();
        logger.info("Exported transactions to: {}", exportPath);
        
        var importResult = importExportService.importTransactionsFromJson(exportPath);
        var importedTransactions = importResult.get();
        logger.info("Imported {} transactions from JSON", importedTransactions.size());
    }
    
    private void demonstrateServiceIntegration() {
        logger.info("--- Demonstrating Service Integration ---");
        
        // Create a enterprise transaction
        var enterpriseTransaction = new Transaction(
                "ENTERPRISE-" + System.currentTimeMillis(),
                new BigDecimal("5000.00"),
                "USD",
                LocalDateTime.now(),
                """
                Enterprise transaction with text block description:
                - High value transaction
                - Requires special processing
                - Monitored by compliance team
                """,
                List.of("enterprise", "high-value", "monitored")
        );
        
        transactionService.addTransaction(enterpriseTransaction);
        logger.info("Added enterprise transaction: {}", enterpriseTransaction.id());
        
        // Demonstrate search and filtering
        var highValueTransactions = transactionService.getTransactionsByAmountRange(
                new BigDecimal("1000"), new BigDecimal("10000")
        );
        
        logger.info("Found {} high-value transactions", highValueTransactions.size());
        
        // Demonstrate metrics
        logger.info("Current transaction count: {}", 
                   transactionService.getTransactionCount());
        
        // Show final metrics
        logger.info("=== Final Application Metrics ===");
        logger.info("All operations completed successfully");
    }
}
