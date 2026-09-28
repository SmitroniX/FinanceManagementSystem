package com.finvantage.service;

import com.finvantage.model.Transaction;
import com.finvantage.util.CurrencyFormatter;
import com.finvantage.util.ValidationUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportService computes analytical aggregations and handles CSV report exports.
 */
public class ReportService {

    private static final ReportService INSTANCE = new ReportService();
    private final FinanceService financeService = FinanceService.getInstance();

    private ReportService() {}

    public static ReportService getInstance() {
        return INSTANCE;
    }

    /**
     * Calculates category-wise expense breakdown percentages for visual charts and analytics.
     */
    public Map<String, CategoryMetric> getCategoryExpenseBreakdown(Long userId) throws SQLException {
        List<Transaction> transactions = financeService.getRecentTransactions(userId, 500);
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            if ("EXPENSE".equalsIgnoreCase(tx.getTransactionType())) {
                String cat = tx.getCategoryName() != null ? tx.getCategoryName() : "Uncategorized";
                BigDecimal current = categoryTotals.getOrDefault(cat, BigDecimal.ZERO);
                categoryTotals.put(cat, current.add(tx.getAmount()));
                totalExpenses = totalExpenses.add(tx.getAmount());
            }
        }

        Map<String, CategoryMetric> results = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            double pct = 0.0;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                pct = entry.getValue()
                        .divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
            results.put(entry.getKey(), new CategoryMetric(entry.getKey(), entry.getValue(), pct));
        }

        return results;
    }

    /**
     * Exports transaction history to standard comma-separated values (CSV) format.
     */
    public File exportTransactionsToCSV(Long userId, File destinationFile) throws IOException, SQLException {
        List<Transaction> txs = financeService.getRecentTransactions(userId, 1000);
        try (PrintWriter writer = new PrintWriter(new FileWriter(destinationFile))) {
            // Write CSV Header
            writer.println("Transaction Reference,Date,Type,Category,Amount,Description,Status");
            for (Transaction tx : txs) {
                String cat = tx.getCategoryName() != null ? tx.getCategoryName().replace(",", " ") : "N/A";
                String desc = tx.getDescription() != null ? tx.getDescription().replace(",", " ") : "";
                writer.printf("%s,%s,%s,%s,%.2f,%s,%s%n",
                    tx.getTransactionRef(),
                    tx.getTransactionDate().format(ValidationUtil.DATE_TIME_FORMATTER),
                    tx.getTransactionType(),
                    cat,
                    tx.getAmount().doubleValue(),
                    desc,
                    tx.getStatus()
                );
            }
        }
        return destinationFile;
    }

    public static class CategoryMetric {
        private final String categoryName;
        private final BigDecimal totalAmount;
        private final double percentage;

        public CategoryMetric(String categoryName, BigDecimal totalAmount, double percentage) {
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
            this.percentage = percentage;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public double getPercentage() {
            return percentage;
        }
    }
}
