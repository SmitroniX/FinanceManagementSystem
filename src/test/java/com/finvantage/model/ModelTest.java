package com.finvantage.model;

import com.finvantage.factory.AccountFactory;
import com.finvantage.factory.TransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying domain entity encapsulation, inheritance, and polymorphism.
 */
public class ModelTest {

    private SavingsAccount savings;
    private CheckingAccount checking;
    private InvestmentAccount investment;

    @BeforeEach
    void setUp() {
        savings = (SavingsAccount) AccountFactory.createAccount(
            "SAVINGS", 1L, 1L, "ACC-SAV-001", "College Fund",
            BigDecimal.valueOf(1000.00), "USD", BigDecimal.valueOf(5.00),
            BigDecimal.ZERO, BigDecimal.valueOf(200.00), null
        );

        checking = (CheckingAccount) AccountFactory.createAccount(
            "CHECKING", 2L, 1L, "ACC-CHK-002", "Operating Account",
            BigDecimal.valueOf(500.00), "USD", BigDecimal.ZERO,
            BigDecimal.valueOf(1000.00), BigDecimal.ZERO, null
        );

        investment = (InvestmentAccount) AccountFactory.createAccount(
            "INVESTMENT", 3L, 1L, "ACC-INV-003", "Growth Portfolio",
            BigDecimal.valueOf(10000.00), "USD", BigDecimal.valueOf(6.00),
            BigDecimal.ZERO, BigDecimal.ZERO, "MODERATE"
        );
    }

    @Test
    @DisplayName("Savings Account: Enforces Minimum Balance Rule")
    void testSavingsMinimumBalance() {
        // Can withdraw within limit
        assertDoesNotThrow(() -> savings.withdraw(BigDecimal.valueOf(700.00)));
        assertEquals(BigDecimal.valueOf(300.00).setScale(2), savings.getBalance());

        // Violates minimum balance of $200.00
        assertThrows(InsufficientFundsException.class, () -> savings.withdraw(BigDecimal.valueOf(150.00)));
    }

    @Test
    @DisplayName("Checking Account: Allows Overdraft up to Approved Limit")
    void testCheckingOverdraft() {
        // Balance is 500, overdraft limit is 1000. Total available = 1500.
        assertDoesNotThrow(() -> checking.withdraw(BigDecimal.valueOf(1200.00)));
        assertEquals(BigDecimal.valueOf(-700.00).setScale(2), checking.getBalance());

        // Exceeds overdraft limit
        assertThrows(InsufficientFundsException.class, () -> checking.withdraw(BigDecimal.valueOf(400.00)));
    }

    @Test
    @DisplayName("Polymorphism: calculateMonthlyYieldOrFee dynamic dispatch")
    void testPolymorphicYield() {
        // Savings gains positive interest: 1000 * 0.05 / 12 = 4.17
        BigDecimal savingsYield = savings.calculateMonthlyYieldOrFee();
        assertTrue(savingsYield.compareTo(BigDecimal.ZERO) > 0);

        // Investment dividend: 10000 * 0.06 / 12 = 50.00
        BigDecimal invDividend = investment.calculateMonthlyYieldOrFee();
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), invDividend);
    }

    @Test
    @DisplayName("Polymorphism: Transaction execute applies atomic debit and credit")
    void testPolymorphicTransactionExecution() throws Exception {
        Transaction transfer = TransactionFactory.createTransaction(
            "TRANSFER", 1L, "TX-TEST-001", checking.getId(), savings.getId(),
            null, BigDecimal.valueOf(200.00), "College savings allocation"
        );

        transfer.execute(checking, savings);

        assertEquals(BigDecimal.valueOf(300.00).setScale(2), checking.getBalance());
        assertEquals(BigDecimal.valueOf(1200.00).setScale(2), savings.getBalance());
    }

    @Test
    @DisplayName("Budget: Correctly calculates utilization and alert states")
    void testBudgetCalculations() {
        Budget b = new Budget(1L, 1L, 10L, "2026-09", BigDecimal.valueOf(1000.00), BigDecimal.valueOf(80.00));
        b.setActualSpent(BigDecimal.valueOf(850.00));

        assertEquals(85.0, b.getUtilizationPercentage(), 0.01);
        assertTrue(b.isWarning());
        assertFalse(b.isExceeded());
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), b.getRemainingAmount());

        // Exceed budget
        b.setActualSpent(BigDecimal.valueOf(1050.00));
        assertTrue(b.isExceeded());
    }
}
