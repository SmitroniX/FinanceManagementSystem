package com.finvantage.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SavingsAccount represents an interest-bearing account enforcing minimum balance requirements.
 * Demonstrates OOP Inheritance and Polymorphism.
 */
public class SavingsAccount extends Account {

    private BigDecimal interestRate = BigDecimal.valueOf(4.50); // 4.50% APY
    private BigDecimal minimumBalance = BigDecimal.valueOf(100.00);

    public SavingsAccount() {
        super();
    }

    public SavingsAccount(Long id, Long userId, String accountNumber, String accountName, 
                          BigDecimal initialBalance, String currency, BigDecimal interestRate, BigDecimal minimumBalance) {
        super(id, userId, accountNumber, accountName, initialBalance, currency);
        if (interestRate != null) this.interestRate = interestRate;
        if (minimumBalance != null) this.minimumBalance = minimumBalance;
    }

    @Override
    public synchronized void withdraw(BigDecimal amount) throws InsufficientFundsException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        BigDecimal prospectiveBalance = getBalance().subtract(amount);
        if (prospectiveBalance.compareTo(minimumBalance) < 0) {
            throw new InsufficientFundsException(
                "Withdrawal violates minimum balance constraint of $" + minimumBalance,
                getAccountNumber(), getBalance(), amount
            );
        }
        setBalance(prospectiveBalance);
    }

    @Override
    public BigDecimal calculateMonthlyYieldOrFee() {
        if (getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // Monthly interest = Balance * (annual rate / 100) / 12
        return getBalance()
                .multiply(interestRate)
                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public String getAccountSummary() {
        return String.format("Savings Account: %s (APY: %.2f%%, Min Bal: $%.2f)", 
                getAccountName(), interestRate.doubleValue(), minimumBalance.doubleValue());
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate != null ? interestRate : BigDecimal.ZERO;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = minimumBalance != null ? minimumBalance : BigDecimal.ZERO;
    }
}
