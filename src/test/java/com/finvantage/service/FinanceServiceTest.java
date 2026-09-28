package com.finvantage.service;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.model.Account;
import com.finvantage.model.FinancialSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service-level unit tests verifying business transactions, balance mutations,
 * and financial summary computations.
 */
public class FinanceServiceTest {

    private final FinanceService financeService = FinanceService.getInstance();

    @BeforeEach
    void setUp() {
        // Ensure in-memory mock mode for fast and deterministic unit testing
        DatabaseConfig.getInstance().setMode("MOCK");
    }

    @Test
    @DisplayName("Service: Records Income and updates account balance")
    void testRecordIncome() throws Exception {
        List<Account> accounts = financeService.getAccounts(1L);
        Account targetAcc = accounts.get(0);
        BigDecimal initialBal = targetAcc.getBalance();

        financeService.recordIncome(targetAcc.getId(), 1L, BigDecimal.valueOf(500.00), "Consulting gig");

        Account updatedAcc = financeService.getAccounts(1L).get(0);
        assertEquals(initialBal.add(BigDecimal.valueOf(500.00)), updatedAcc.getBalance());
    }

    @Test
    @DisplayName("Service: Executes Atomic Transfer between two accounts")
    void testAtomicTransfer() throws Exception {
        List<Account> accounts = financeService.getAccounts(1L);
        Account src = accounts.get(1); // Checking
        Account dst = accounts.get(0); // Savings

        BigDecimal srcBefore = src.getBalance();
        BigDecimal dstBefore = dst.getBalance();
        BigDecimal transferAmount = BigDecimal.valueOf(300.00);

        financeService.executeAtomicTransfer(src.getId(), dst.getId(), transferAmount, "Emergency savings deposit");

        assertEquals(srcBefore.subtract(transferAmount), src.getBalance());
        assertEquals(dstBefore.add(transferAmount), dst.getBalance());
    }

    @Test
    @DisplayName("Service: Computes accurate Financial Summary KPIs")
    void testFinancialSummary() throws Exception {
        FinancialSummary summary = financeService.getFinancialSummary(1L);

        assertNotNull(summary);
        assertTrue(summary.getTotalNetWorth().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(summary.getActiveAccountsCount() >= 3);
        assertTrue(summary.getTotalTransactionsCount() > 0);
    }
}
