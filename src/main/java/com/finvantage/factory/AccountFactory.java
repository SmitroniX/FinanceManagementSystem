package com.finvantage.factory;

import com.finvantage.model.Account;
import com.finvantage.model.CheckingAccount;
import com.finvantage.model.InvestmentAccount;
import com.finvantage.model.SavingsAccount;

import java.math.BigDecimal;

/**
 * Factory Pattern implementation for instantiating polymorphic Account subclasses.
 * Decouples account creation logic from database mappers and user interface controllers.
 */
public class AccountFactory {

    public static Account createAccount(String accountType, Long id, Long userId, 
                                        String accountNumber, String accountName, 
                                        BigDecimal balance, String currency, 
                                        BigDecimal interestRate, BigDecimal overdraftLimit, 
                                        BigDecimal minimumBalance, String portfolioRisk) {
        if (accountType == null) {
            throw new IllegalArgumentException("Account type cannot be null.");
        }

        switch (accountType.toUpperCase().trim()) {
            case "SAVINGS":
                return new SavingsAccount(
                    id, userId, accountNumber, accountName, balance, currency,
                    interestRate != null ? interestRate : BigDecimal.valueOf(4.50),
                    minimumBalance != null ? minimumBalance : BigDecimal.valueOf(100.00)
                );
            case "CHECKING":
                return new CheckingAccount(
                    id, userId, accountNumber, accountName, balance, currency,
                    overdraftLimit != null ? overdraftLimit : BigDecimal.valueOf(1000.00)
                );
            case "INVESTMENT":
                return new InvestmentAccount(
                    id, userId, accountNumber, accountName, balance, currency,
                    interestRate != null ? interestRate : BigDecimal.valueOf(7.00),
                    portfolioRisk != null ? portfolioRisk : "MODERATE"
                );
            default:
                throw new IllegalArgumentException("Unsupported account type: " + accountType);
        }
    }
}
