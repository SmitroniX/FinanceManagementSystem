package com.finvantage.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Budget models spending limits allocated to categories for a specific billing month.
 * Encapsulates dynamic budget health calculation.
 */
public class Budget extends BaseEntity {

    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String categoryColor = "#4A90E2";
    private String monthYear; // e.g. "2026-09"
    private BigDecimal budgetLimit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal actualSpent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal alertThreshold = BigDecimal.valueOf(80.00); // 80%

    public Budget() {
        super();
    }

    public Budget(Long id, Long userId, Long categoryId, String monthYear, BigDecimal budgetLimit, BigDecimal alertThreshold) {
        super(id);
        this.userId = userId;
        this.categoryId = categoryId;
        this.monthYear = monthYear;
        if (budgetLimit != null) this.budgetLimit = budgetLimit.setScale(2, RoundingMode.HALF_UP);
        if (alertThreshold != null) this.alertThreshold = alertThreshold;
    }

    public BigDecimal getRemainingAmount() {
        return budgetLimit.subtract(actualSpent).setScale(2, RoundingMode.HALF_UP);
    }

    public double getUtilizationPercentage() {
        if (budgetLimit.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return actualSpent
                .divide(budgetLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public boolean isExceeded() {
        return actualSpent.compareTo(budgetLimit) > 0;
    }

    public boolean isWarning() {
        double pct = getUtilizationPercentage();
        return pct >= alertThreshold.doubleValue() && pct <= 100.0;
    }

    public String getStatusBadge() {
        if (isExceeded()) {
            return "OVER BUDGET";
        } else if (isWarning()) {
            return "NEAR LIMIT (" + Math.round(getUtilizationPercentage()) + "%)";
        } else {
            return "HEALTHY (" + Math.round(getUtilizationPercentage()) + "%)";
        }
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit != null ? budgetLimit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getActualSpent() {
        return actualSpent;
    }

    public void setActualSpent(BigDecimal actualSpent) {
        this.actualSpent = actualSpent != null ? actualSpent.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(BigDecimal alertThreshold) {
        this.alertThreshold = alertThreshold != null ? alertThreshold : BigDecimal.valueOf(80.00);
    }
}
