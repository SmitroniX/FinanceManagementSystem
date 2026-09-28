package com.finvantage.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Abstract Transaction class modeling the financial movements in the double-entry ledger.
 * Demonstrates:
 * 1. Abstraction: Contract for executing financial state mutations.
 * 2. Polymorphism: Income, Expense, and Transfer execute distinct balance mutations.
 * 3. Encapsulation: Protects amounts and reference IDs from invalid state.
 */
public abstract class Transaction extends BaseEntity {

    private String transactionRef;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private LocalDateTime transactionDate;
    private String description;
    private String status = "COMPLETED";

    public Transaction() {
        super();
        this.transactionDate = LocalDateTime.now();
    }

    public Transaction(Long id, String transactionRef, Long sourceAccountId, Long destinationAccountId,
                       Long categoryId, BigDecimal amount, String description) {
        super(id);
        this.transactionRef = transactionRef;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.categoryId = categoryId;
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Transaction amount must be strictly greater than zero.");
            }
            this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
        this.description = description;
        this.transactionDate = LocalDateTime.now();
    }

    // Abstract methods for polymorphic execution
    public abstract void execute(Account sourceAccount, Account destinationAccount) throws Exception;
    public abstract String getTransactionType();

    // Getters and Setters
    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate != null ? transactionDate : LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + getTransactionType() + "] " + transactionRef + " : $" + amount + " (" + description + ")";
    }
}
