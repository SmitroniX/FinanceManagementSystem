package com.finvantage.factory;

import com.finvantage.model.ExpenseTransaction;
import com.finvantage.model.IncomeTransaction;
import com.finvantage.model.Transaction;
import com.finvantage.model.TransferTransaction;

import java.math.BigDecimal;

/**
 * Factory Pattern implementation for instantiating polymorphic Transaction subclasses.
 * Enforces clean separation between transaction types and persistence mappers.
 */
public class TransactionFactory {

    public static Transaction createTransaction(String txType, Long id, String transactionRef,
                                                Long sourceAccountId, Long destinationAccountId,
                                                Long categoryId, BigDecimal amount, String description) {
        if (txType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null.");
        }

        switch (txType.toUpperCase().trim()) {
            case "INCOME":
                return new IncomeTransaction(id, transactionRef, destinationAccountId, categoryId, amount, description);
            case "EXPENSE":
                return new ExpenseTransaction(id, transactionRef, sourceAccountId, categoryId, amount, description);
            case "TRANSFER":
                return new TransferTransaction(id, transactionRef, sourceAccountId, destinationAccountId, amount, description);
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + txType);
        }
    }
}
