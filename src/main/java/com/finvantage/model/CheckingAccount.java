package com.finvantage.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CheckingAccount provides everyday operational liquidity with overdraft protection.
 * Demonstrates OOP Polymorphism and business constraint enforcement.
 */
public class CheckingAccount extends Account {

    private BigDecimal overdraftLimit = BigDecimal.valueOf(1000.00);
    private BigDecimal overdraftFee = BigDecimal.valueOf(25.00);

    public CheckingAccount() {
        super();
    }

    public CheckingAccount(Long id, Long userId, String accountNumber, String accountName, 
                           BigDecimal initialBalance, String currency, BigDecimal overdraftLimit) {
        super(id, userId, accountNumber, accountName, initialBalance, currency);
        if (overdraftLimit != null) this.overdraftLimit = overdraftLimit;
    }

    @Override
    public synchronized void withdraw(BigDecimal amount) throws InsufficientFundsException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        BigDecimal prospectiveBalance = getBalance().subtract(amount);
        BigDecimal maxAllowableDebt = overdraftLimit.negate();
        if (prospectiveBalance.compareTo(maxAllowableDebt) < 0) {
            throw new InsufficientFundsException(
                "Withdrawal exceeds approved overdraft limit of $" + overdraftLimit,
                getAccountNumber(), getBalance(), amount
            );
        }
        setBalance(prospectiveBalance);
    }

    @Override
    public BigDecimal calculateMonthlyYieldOrFee() {
        // If account is overdrawn, assess monthly negative penalty fee
        if (getBalance().compareTo(BigDecimal.ZERO) < 0) {
            return overdraftFee.negate().setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getAccountType() {
        return "CHECKING";
    }

    @Override
    public String getAccountSummary() {
        return String.format("Checking Account: %s (Overdraft Protection: $%.2f)", 
                getAccountName(), overdraftLimit.doubleValue());
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit != null ? overdraftLimit : BigDecimal.ZERO;
    }

    public BigDecimal getOverdraftFee() {
        return overdraftFee;
    }

    public void setOverdraftFee(BigDecimal overdraftFee) {
        this.overdraftFee = overdraftFee != null ? overdraftFee : BigDecimal.valueOf(25.00);
    }
}
