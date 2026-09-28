package com.finvantage.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility for uniform monetary formatting across the Swing UI and reports.
 */
public class CurrencyFormatter {

    private static final NumberFormat USD_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    public static String formatUSD(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return USD_FORMAT.format(amount);
    }

    public static String formatSignedUSD(BigDecimal amount, String txType) {
        if (amount == null) return "$0.00";
        String formatted = USD_FORMAT.format(amount);
        if ("INCOME".equalsIgnoreCase(txType)) {
            return "+" + formatted;
        } else if ("EXPENSE".equalsIgnoreCase(txType)) {
            return "-" + formatted;
        } else {
            return "⇆ " + formatted;
        }
    }
}
