package com.example.util;

import java.util.concurrent.*;
import java.util.List;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Concurrent processing utilities using modern Java patterns
 */
public final class ConcurrentUtils {
    
    private static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();
    private static final AtomicLong threadCounter = new AtomicLong(0);
    
    private ConcurrentUtils() {
        // Utility class
    }
    
    /**
     * Create custom thread pool with proper naming
     */
    public static ExecutorService createNamedThreadPool(String name, int parallelism) {
        return Executors.newFixedThreadPool(parallelism, r -> {
            var thread = new Thread(r);
            thread.setName(name + "-" + threadCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }
    
    /**
     * Process items in parallel with custom parallelism
     */
    public static <T, R> List<R> processInParallel(Collection<T> items,
                                                   Function<T, R> processor,
                                                   int parallelism) {
        if (items.isEmpty()) {
            return List.of();
        }
        
        try (var executor = createNamedThreadPool("parallel-processor", parallelism)) {
            var futures = items.stream()
                    .map(item -> CompletableFuture.supplyAsync(() -> processor.apply(item), executor))
                    .toList();
            
            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Process items in parallel with default parallelism
     */
    public static <T, R> List<R> processInParallel(Collection<T> items,
                                                   Function<T, R> processor) {
        return processInParallel(items, processor, DEFAULT_PARALLELISM);
    }
    
    /**
     * Process items with parallel streams (modern approach)
     */
    public static <T, R> List<R> processWithParallelStream(Collection<T> items,
                                                           Function<T, R> processor) {
        return items.parallelStream()
                .map(processor)
                .collect(Collectors.toList());
    }
    
    /**
     * Batch processing with configurable batch size
     */
    public static <T, R> List<R> processByBatches(Collection<T> items,
                                                  Function<List<T>, List<R>> batchProcessor,
                                                  int batchSize) {
        var itemsList = List.copyOf(items);
        var batches = partition(itemsList, batchSize);
        
        return batches.parallelStream()
                .map(batchProcessor)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
    
    /**
     * Partition collection into batches
     */
    private static <T> List<List<T>> partition(List<T> list, int batchSize) {
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(
                        i * batchSize,
                        Math.min((i + 1) * batchSize, list.size())
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Combine multiple futures with timeout
     */
    public static <T> CompletableFuture<List<T>> combineWithTimeout(
            List<CompletableFuture<T>> futures,
            long timeoutMillis) {
        
        var combined = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        
        return combined.thenApply(v -> 
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        ).orTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
    }
}
