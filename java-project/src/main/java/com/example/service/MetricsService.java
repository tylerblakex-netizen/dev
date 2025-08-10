package com.example.service;

import com.example.model.Transaction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics service for application monitoring
 */
public class MetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    
    private final MeterRegistry meterRegistry;
    private final Counter transactionSavedCounter;
    private final Counter transactionDeletedCounter;
    private final Timer transactionProcessingTimer;
    private final Map<String, AtomicLong> customMetrics;
    
    public MetricsService() {
        this.meterRegistry = new SimpleMeterRegistry();
        this.transactionSavedCounter = Counter.builder("transactions.saved")
                .description("Number of transactions saved")
                .register(meterRegistry);
        this.transactionDeletedCounter = Counter.builder("transactions.deleted")
                .description("Number of transactions deleted")
                .register(meterRegistry);
        this.transactionProcessingTimer = Timer.builder("transaction.processing.time")
                .description("Transaction processing time")
                .register(meterRegistry);
        this.customMetrics = new ConcurrentHashMap<>();
    }
    
    public void recordTransactionSaved(Transaction transaction) {
        transactionSavedCounter.increment();
        incrementCustomMetric("transactions.by.type." + transaction.type().getCode());
        
        logger.debug("Recorded transaction saved metric for: {}", transaction.id());
    }
    
    public void recordTransactionDeleted(Transaction transaction) {
        transactionDeletedCounter.increment();
        
        logger.debug("Recorded transaction deleted metric for: {}", transaction.id());
    }
    
    public void recordProcessingTime(Duration duration) {
        transactionProcessingTimer.record(duration);
    }
    
    public void incrementCustomMetric(String metricName) {
        customMetrics.computeIfAbsent(metricName, k -> new AtomicLong(0))
                     .incrementAndGet();
    }
    
    public Map<String, Object> getAllMetrics() {
        var metrics = new java.util.HashMap<String, Object>();
        
        // Built-in metrics
        metrics.put("transactions.saved", transactionSavedCounter.count());
        metrics.put("transactions.deleted", transactionDeletedCounter.count());
        metrics.put("transaction.processing.time.count", transactionProcessingTimer.count());
        metrics.put("transaction.processing.time.total", transactionProcessingTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS));
        
        // Custom metrics
        customMetrics.forEach((key, value) -> metrics.put(key, value.get()));
        
        return metrics;
    }
    
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
