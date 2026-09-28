package com.finvantage.model;

import java.math.BigDecimal;

/**
 * Custom checked exception thrown when an account withdrawal or transfer
 * exceeds allowed balances or overdraft thresholds.
 * Demonstrates OOP Custom Exception Handling.
 */
public class InsufficientFundsException extends Exception {

    private final String accountNumber;
    private final BigDecimal currentBalance;
    private final BigDecimal attemptedAmount;

    public InsufficientFundsException(String message, String accountNumber, BigDecimal currentBalance, BigDecimal attemptedAmount) {
        super(message);
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.attemptedAmount = attemptedAmount;
    }

    public InsufficientFundsException(String message) {
        super(message);
        this.accountNumber = "N/A";
        this.currentBalance = BigDecimal.ZERO;
        this.attemptedAmount = BigDecimal.ZERO;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getAttemptedAmount() {
        return attemptedAmount;
    }

    @Override
    public String toString() {
        return "InsufficientFundsException{" +
                "accountNumber='" + accountNumber + '\'' +
                ", currentBalance=" + currentBalance +
                ", attemptedAmount=" + attemptedAmount +
                ", message=" + getMessage() +
                '}';
    }
}
