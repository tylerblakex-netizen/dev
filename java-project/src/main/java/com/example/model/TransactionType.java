package com.example.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing transaction types with enhanced capabilities
 */
public enum TransactionType {
    DEBIT("debit", "Money going out"),
    CREDIT("credit", "Money coming in"), 
    TRANSFER("transfer", "Money moving between accounts");
    
    private final String code;
    private final String description;
    
    TransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Find transaction type by code using modern switch expression
     */
    public static TransactionType fromCode(String code) {
        return switch (code.toLowerCase()) {
            case "debit" -> DEBIT;
            case "credit" -> CREDIT;
            case "transfer" -> TRANSFER;
            default -> throw new IllegalArgumentException("Unknown transaction type: " + code);
        };
    }
    
    /**
     * Check if this is an outgoing transaction
     */
    public boolean isOutgoing() {
        return this == DEBIT;
    }
    
    /**
     * Check if this is an incoming transaction  
     */
    public boolean isIncoming() {
        return this == CREDIT;
    }
}
