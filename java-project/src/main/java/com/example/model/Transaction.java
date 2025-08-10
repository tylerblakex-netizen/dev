package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Modern Java 21 record representing a financial transaction
 * Demonstrates records, validation, and immutability
 */
public record Transaction(
    @NotNull
    @JsonProperty("id")
    String id,
    
    @NotBlank
    @Size(min = 1, max = 100)
    @JsonProperty("description")
    String description,
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    @JsonProperty("amount")
    BigDecimal amount,
    
    @NotNull
    @JsonProperty("timestamp")
    LocalDateTime timestamp,
    
    @NotNull
    @JsonProperty("type")
    TransactionType type,
    
    @NotNull
    @JsonProperty("tags")
    List<@NotBlank String> tags,
    
    @JsonProperty("metadata")
    TransactionMetadata metadata
) {
    
    @JsonCreator
    public Transaction {
        // Compact constructor for validation and normalization
        Objects.requireNonNull(id, "Transaction ID cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        Objects.requireNonNull(type, "Transaction type cannot be null");
        Objects.requireNonNull(tags, "Tags cannot be null");
        
        // Normalize and validate
        description = description.trim();
        tags = List.copyOf(tags); // Ensure immutability
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
    
    /**
     * Check if transaction is recent (within last 24 hours)
     */
    public boolean isRecent() {
        return timestamp.isAfter(LocalDateTime.now().minusDays(1));
    }
    
    /**
     * Get formatted amount string
     */
    public String getFormattedAmount() {
        return switch (type) {
            case DEBIT -> "-$" + amount;
            case CREDIT -> "+$" + amount;
            case TRANSFER -> "$" + amount;
        };
    }
    
    /**
     * Create a copy with updated metadata
     */
    public Transaction withMetadata(TransactionMetadata newMetadata) {
        return new Transaction(id, description, amount, timestamp, type, tags, newMetadata);
    }
}
