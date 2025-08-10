package com.example.service;

import com.example.model.Transaction;
import com.example.model.TransactionType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * High-performance transaction service using modern Java features
 */
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private final Cache<String, Transaction> cache;
    private final MetricsService metricsService;
    
    public TransactionService(MetricsService metricsService) {
        this.metricsService = Objects.requireNonNull(metricsService);
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
                .build();
    }
    
    /**
     * Save transaction asynchronously
     */
    public CompletableFuture<Transaction> saveAsync(Transaction transaction) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Saving transaction: {}", transaction.id());
            
            var saved = transactions.put(transaction.id(), transaction);
            cache.put(transaction.id(), transaction);
            
            metricsService.recordTransactionSaved(transaction);
            
            logger.info("Transaction saved successfully: {}", transaction.id());
            return transaction;
        });
    }
    
    /**
     * Find transaction by ID with caching
     */
    public Optional<Transaction> findById(String id) {
        var cached = cache.getIfPresent(id);
        if (cached != null) {
            logger.debug("Cache hit for transaction: {}", id);
            return Optional.of(cached);
        }
        
        var transaction = Optional.ofNullable(transactions.get(id));
        transaction.ifPresent(t -> cache.put(id, t));
        
        logger.debug("Cache miss for transaction: {}", id);
        return transaction;
    }
    
    /**
     * Find transactions using modern stream operations
     */
    public Stream<Transaction> findTransactions(TransactionFilter filter) {
        return transactions.values()
                .parallelStream()
                .filter(filter.toPredicate())
                .sorted(Comparator.comparing(Transaction::timestamp).reversed());
    }
    
    /**
     * Calculate balance using streams and reduce
     */
    public BigDecimal calculateBalance() {
        return transactions.values()
                .parallelStream()
                .map(this::getSignedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get recent transactions (last 24 hours)
     */
    public List<Transaction> getRecentTransactions() {
        return findTransactions(TransactionFilter.recent())
                .limit(100)
                .toList();
    }
    
    /**
     * Get transaction statistics
     */
    public TransactionStats getStats() {
        var allTransactions = transactions.values();
        
        var totalCount = allTransactions.size();
        var totalAmount = allTransactions.parallelStream()
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        var avgAmount = totalCount > 0 
                ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        var typeDistribution = allTransactions.parallelStream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Transaction::type,
                    java.util.stream.Collectors.counting()
                ));
        
        return new TransactionStats(totalCount, totalAmount, avgAmount, typeDistribution);
    }
    
    /**
     * Bulk save transactions with parallel processing
     */
    public CompletableFuture<List<Transaction>> saveBulkAsync(List<Transaction> transactionList) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Saving {} transactions in bulk", transactionList.size());
            
            var results = transactionList.parallelStream()
                    .peek(t -> {
                        transactions.put(t.id(), t);
                        cache.put(t.id(), t);
                        metricsService.recordTransactionSaved(t);
                    })
                    .toList();
            
            logger.info("Bulk save completed: {} transactions", results.size());
            return results;
        });
    }
    
    /**
     * Delete transaction
     */
    public boolean deleteTransaction(String id) {
        var removed = transactions.remove(id);
        cache.invalidate(id);
        
        if (removed != null) {
            metricsService.recordTransactionDeleted(removed);
            logger.info("Transaction deleted: {}", id);
            return true;
        }
        
        logger.warn("Transaction not found for deletion: {}", id);
        return false;
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        var stats = cache.stats();
        return Map.of(
            "hitCount", stats.hitCount(),
            "missCount", stats.missCount(),
            "hitRate", stats.hitRate(),
            "evictionCount", stats.evictionCount(),
            "estimatedSize", cache.estimatedSize()
        );
    }
    
    private BigDecimal getSignedAmount(Transaction transaction) {
        return switch (transaction.type()) {
            case DEBIT -> transaction.amount().negate();
            case CREDIT, TRANSFER -> transaction.amount();
        };
    }
    
    /**
     * Transaction filter using modern builder pattern
     */
    public static class TransactionFilter {
        private final List<Predicate<Transaction>> predicates = new ArrayList<>();
        
        public static TransactionFilter builder() {
            return new TransactionFilter();
        }
        
        public static TransactionFilter recent() {
            return builder().after(LocalDateTime.now().minusDays(1));
        }
        
        public TransactionFilter withType(TransactionType type) {
            predicates.add(t -> t.type() == type);
            return this;
        }
        
        public TransactionFilter after(LocalDateTime dateTime) {
            predicates.add(t -> t.timestamp().isAfter(dateTime));
            return this;
        }
        
        public TransactionFilter before(LocalDateTime dateTime) {
            predicates.add(t -> t.timestamp().isBefore(dateTime));
            return this;
        }
        
        public TransactionFilter amountGreaterThan(BigDecimal amount) {
            predicates.add(t -> t.amount().compareTo(amount) > 0);
            return this;
        }
        
        public TransactionFilter withTag(String tag) {
            predicates.add(t -> t.tags().contains(tag));
            return this;
        }
        
        public Predicate<Transaction> toPredicate() {
            return predicates.stream()
                    .reduce(Predicate::and)
                    .orElse(t -> true);
        }
    }
    
    /**
     * Transaction statistics record
     */
    public record TransactionStats(
        int totalCount,
        BigDecimal totalAmount,
        BigDecimal averageAmount,
        Map<TransactionType, Long> typeDistribution
    ) {}
}
