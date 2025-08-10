package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;

/**
 * Transaction metadata using modern Java features
 */
public record TransactionMetadata(
    @JsonProperty("source")
    Optional<String> source,
    
    @JsonProperty("category")
    Optional<String> category,
    
    @JsonProperty("properties")
    Map<String, Object> properties
) {
    
    public TransactionMetadata {
        // Ensure immutability
        properties = properties != null ? Map.copyOf(properties) : Map.of();
    }
    
    /**
     * Create empty metadata
     */
    public static TransactionMetadata empty() {
        return new TransactionMetadata(Optional.empty(), Optional.empty(), Map.of());
    }
    
    /**
     * Create metadata with source
     */
    public static TransactionMetadata withSource(String source) {
        return new TransactionMetadata(Optional.of(source), Optional.empty(), Map.of());
    }
    
    /**
     * Get property value by key
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProperty(String key, Class<T> type) {
        return Optional.ofNullable(properties.get(key))
                      .filter(type::isInstance)
                      .map(type::cast);
    }
    
    /**
     * Add or update a property
     */
    public TransactionMetadata withProperty(String key, Object value) {
        var newProperties = new java.util.HashMap<>(properties);
        newProperties.put(key, value);
        return new TransactionMetadata(source, category, newProperties);
    }
}
