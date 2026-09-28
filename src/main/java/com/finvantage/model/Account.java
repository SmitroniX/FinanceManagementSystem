package com.finvantage.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Abstract Account class serving as the base for all banking and investment accounts.
 * Demonstrates:
 * 1. Abstraction: Defines contract for deposits, withdrawals, and yield calculations.
 * 2. Polymorphism: Subclasses implement type-specific financial rules.
 * 3. Encapsulation: Protects internal balance state from direct negative modification.
 */
public abstract class Account extends BaseEntity {

    private Long userId;
    private String accountNumber;
    private String accountName;
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private String currency = "USD";
    private String status = "ACTIVE";

    public Account() {
        super();
    }

    public Account(Long id, Long userId, String accountNumber, String accountName, BigDecimal initialBalance, String currency) {
        super(id);
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        if (initialBalance != null) {
            this.balance = initialBalance.setScale(2, RoundingMode.HALF_UP);
        }
        if (currency != null) {
            this.currency = currency.toUpperCase();
        }
    }

    // Abstract Methods for Polymorphism
    public abstract void withdraw(BigDecimal amount) throws InsufficientFundsException;
    public abstract BigDecimal calculateMonthlyYieldOrFee();
    public abstract String getAccountType();
    public abstract String getAccountSummary();

    /**
     * Common deposit logic with strict positive input validation.
     */
    public synchronized void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly greater than zero.");
        }
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return accountName + " (" + getAccountType() + " - " + accountNumber + ") - $" + balance;
    }
}
