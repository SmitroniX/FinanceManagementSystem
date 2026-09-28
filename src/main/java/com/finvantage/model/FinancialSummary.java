package com.finvantage.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * FinancialSummary is an immutable Data Transfer Object (DTO) aggregating
 * high-level metrics for dashboard cards and analytical evaluation.
 */
public class FinancialSummary {

    private final BigDecimal totalNetWorth;
    private final BigDecimal monthlyIncome;
    private final BigDecimal monthlyExpense;
    private final BigDecimal netSavings;
    private final double savingsRate;
    private final int activeAccountsCount;
    private final int totalTransactionsCount;

    public FinancialSummary(BigDecimal totalNetWorth, BigDecimal monthlyIncome, 
                            BigDecimal monthlyExpense, int activeAccountsCount, int totalTransactionsCount) {
        this.totalNetWorth = totalNetWorth != null ? totalNetWorth.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.monthlyIncome = monthlyIncome != null ? monthlyIncome.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.monthlyExpense = monthlyExpense != null ? monthlyExpense.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.netSavings = this.monthlyIncome.subtract(this.monthlyExpense).setScale(2, RoundingMode.HALF_UP);
        
        if (this.monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            this.savingsRate = this.netSavings
                    .divide(this.monthlyIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else {
            this.savingsRate = 0.0;
        }
        
        this.activeAccountsCount = activeAccountsCount;
        this.totalTransactionsCount = totalTransactionsCount;
    }

    public BigDecimal getTotalNetWorth() {
        return totalNetWorth;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public BigDecimal getMonthlyExpense() {
        return monthlyExpense;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public double getSavingsRate() {
        return savingsRate;
    }

    public int getActiveAccountsCount() {
        return activeAccountsCount;
    }

    public int getTotalTransactionsCount() {
        return totalTransactionsCount;
    }
}
