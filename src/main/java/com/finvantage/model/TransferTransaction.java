package com.finvantage.model;

import java.math.BigDecimal;

/**
 * TransferTransaction represents an internal or external funds movement between two accounts.
 * Demonstrates OOP Polymorphism and coordinated atomic state mutations.
 */
public class TransferTransaction extends Transaction {

    public TransferTransaction() {
        super();
    }

    public TransferTransaction(Long id, String transactionRef, Long sourceAccountId, 
                               Long destinationAccountId, BigDecimal amount, String description) {
        super(id, transactionRef, sourceAccountId, destinationAccountId, null, amount, description);
    }

    @Override
    public void execute(Account sourceAccount, Account destinationAccount) throws Exception {
        if (sourceAccount == null || destinationAccount == null) {
            throw new IllegalArgumentException("Both source and destination accounts are required for transfers.");
        }
        if (sourceAccount.getId() != null && sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the identical account.");
        }
        // Step 1: Debit Source (Will throw InsufficientFundsException if rules violated)
        sourceAccount.withdraw(getAmount());
        // Step 2: Credit Destination
        destinationAccount.deposit(getAmount());
    }

    @Override
    public String getTransactionType() {
        return "TRANSFER";
    }
}
