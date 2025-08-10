package com.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Performance monitoring utilities using modern Java features
 */
public final class PerformanceUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceUtils.class);
    
    private PerformanceUtils() {
        // Utility class
    }
    
    /**
     * Time the execution of a supplier and log performance
     */
    public static <T> T timeExecution(String operationName, Supplier<T> operation) {
        var start = Instant.now();
        
        try {
            var result = operation.get();
            var duration = Duration.between(start, Instant.now());
            
            logger.info("Operation '{}' completed in {} ms", 
                       operationName, 
                       duration.toMillis());
            
            return result;
            
        } catch (Exception e) {
            var duration = Duration.between(start, Instant.now());
            logger.error("Operation '{}' failed after {} ms", 
                        operationName, 
                        duration.toMillis(), 
                        e);
            throw e;
        }
    }
    
    /**
     * Time async operation
     */
    public static <T> CompletableFuture<T> timeAsyncExecution(String operationName, 
                                                              Supplier<CompletableFuture<T>> operation) {
        var start = Instant.now();
        
        return operation.get()
                .whenComplete((result, throwable) -> {
                    var duration = Duration.between(start, Instant.now());
                    
                    if (throwable != null) {
                        logger.error("Async operation '{}' failed after {} ms", 
                                   operationName, 
                                   duration.toMillis(), 
                                   throwable);
                    } else {
                        logger.info("Async operation '{}' completed in {} ms", 
                                  operationName, 
                                  duration.toMillis());
                    }
                });
    }
    
    /**
     * Execute with timeout
     */
    public static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> future, 
                                                       Duration timeout) {
        return future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Measure execution time
     */
    public static class ExecutionTimer implements AutoCloseable {
        private final String operationName;
        private final Instant start;
        
        public ExecutionTimer(String operationName) {
            this.operationName = operationName;
            this.start = Instant.now();
            logger.debug("Starting operation: {}", operationName);
        }
        
        @Override
        public void close() {
            var duration = Duration.between(start, Instant.now());
            logger.info("Operation '{}' took {} ms", operationName, duration.toMillis());
        }
    }
}
