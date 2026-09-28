package com.finvantage.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * InvestmentAccount represents capital allocation portfolios (stocks, index funds, bonds).
 * Demonstrates OOP Polymorphism through dividend yield estimation.
 */
public class InvestmentAccount extends Account {

    private BigDecimal annualDividendYield = BigDecimal.valueOf(6.50); // 6.50% estimated yield
    private String portfolioRisk = "MODERATE";

    public InvestmentAccount() {
        super();
    }

    public InvestmentAccount(Long id, Long userId, String accountNumber, String accountName, 
                             BigDecimal initialBalance, String currency, BigDecimal dividendYield, String portfolioRisk) {
        super(id, userId, accountNumber, accountName, initialBalance, currency);
        if (dividendYield != null) this.annualDividendYield = dividendYield;
        if (portfolioRisk != null) this.portfolioRisk = portfolioRisk;
    }

    @Override
    public synchronized void withdraw(BigDecimal amount) throws InsufficientFundsException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                "Insufficient investment liquidity for withdrawal.",
                getAccountNumber(), getBalance(), amount
            );
        }
        setBalance(getBalance().subtract(amount));
    }

    @Override
    public BigDecimal calculateMonthlyYieldOrFee() {
        if (getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // Monthly estimated dividend distribution
        return getBalance()
                .multiply(annualDividendYield)
                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getAccountType() {
        return "INVESTMENT";
    }

    @Override
    public String getAccountSummary() {
        return String.format("Investment Portfolio: %s (Risk: %s, Dividend Yield: %.2f%%)", 
                getAccountName(), portfolioRisk, annualDividendYield.doubleValue());
    }

    public BigDecimal getAnnualDividendYield() {
        return annualDividendYield;
    }

    public void setAnnualDividendYield(BigDecimal annualDividendYield) {
        this.annualDividendYield = annualDividendYield != null ? annualDividendYield : BigDecimal.ZERO;
    }

    public String getPortfolioRisk() {
        return portfolioRisk;
    }

    public void setPortfolioRisk(String portfolioRisk) {
        this.portfolioRisk = portfolioRisk != null ? portfolioRisk : "MODERATE";
    }
}
