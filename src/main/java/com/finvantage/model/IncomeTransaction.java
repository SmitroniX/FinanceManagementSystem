package com.finvantage.model;

import java.math.BigDecimal;

/**
 * IncomeTransaction represents inflow of funds (salary, dividends, interest, sales)
 * credited into a destination account.
 * Demonstrates OOP Polymorphism.
 */
public class IncomeTransaction extends Transaction {

    public IncomeTransaction() {
        super();
    }

    public IncomeTransaction(Long id, String transactionRef, Long destinationAccountId, 
                             Long categoryId, BigDecimal amount, String description) {
        super(id, transactionRef, null, destinationAccountId, categoryId, amount, description);
    }

    @Override
    public void execute(Account sourceAccount, Account destinationAccount) throws Exception {
        if (destinationAccount == null) {
            throw new IllegalArgumentException("Destination account is required for income transactions.");
        }
        destinationAccount.deposit(getAmount());
    }

    @Override
    public String getTransactionType() {
        return "INCOME";
    }
}
