package com.finvantage.model;

import java.math.BigDecimal;

/**
 * ExpenseTransaction represents outflow of funds (bills, rent, food, operational expenses)
 * debited from a source account.
 * Demonstrates OOP Polymorphism and business constraint checking.
 */
public class ExpenseTransaction extends Transaction {

    public ExpenseTransaction() {
        super();
    }

    public ExpenseTransaction(Long id, String transactionRef, Long sourceAccountId, 
                              Long categoryId, BigDecimal amount, String description) {
        super(id, transactionRef, sourceAccountId, null, categoryId, amount, description);
    }

    @Override
    public void execute(Account sourceAccount, Account destinationAccount) throws Exception {
        if (sourceAccount == null) {
            throw new IllegalArgumentException("Source account is required for expense transactions.");
        }
        sourceAccount.withdraw(getAmount());
    }

    @Override
    public String getTransactionType() {
        return "EXPENSE";
    }
}
