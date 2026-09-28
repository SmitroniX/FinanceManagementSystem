package com.finvantage.ui.panels;

import com.finvantage.event.FinanceEventListener;
import com.finvantage.event.FinanceEventManager;
import com.finvantage.model.Account;
import com.finvantage.model.CheckingAccount;
import com.finvantage.model.InvestmentAccount;
import com.finvantage.model.SavingsAccount;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.FinanceService;
import com.finvantage.ui.components.CardPanel;
import com.finvantage.ui.dialogs.AddAccountDialog;
import com.finvantage.util.CurrencyFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel managing polymorphic accounts (Savings, Checking, Investment).
 * Demonstrates OOP Polymorphism through dynamic yield/fee projections and specialized card details.
 */
public class AccountsPanel extends JPanel implements FinanceEventListener {

    private final JFrame parentFrame;
    private final FinanceService financeService = FinanceService.getInstance();
    private final JPanel cardsContainer = new JPanel();

    public AccountsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setBackground(new Color(248, 250, 252));

        initComponents();
        FinanceEventManager.getInstance().addListener(this);
        refreshAccounts();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Accounts & Liquidity Portfolios");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Polymorphic accounts implementing custom interest, overdraft, and dividend contracts");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        titleGroup.add(lblTitle);
        titleGroup.add(lblSubtitle);
        topPanel.add(titleGroup, BorderLayout.WEST);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);

        JButton btnYieldTest = new JButton("Run Polymorphic Yield / Fee Simulation");
        btnYieldTest.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnYieldTest.setBackground(new Color(79, 70, 229));
        btnYieldTest.setForeground(Color.WHITE);
        btnYieldTest.setFocusPainted(false);
        btnYieldTest.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnYieldTest.addActionListener(e -> runPolymorphicYieldSimulation());

        JButton btnNewAcc = new JButton("+ Open New Account");
        btnNewAcc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNewAcc.setBackground(new Color(22, 163, 74));
        btnNewAcc.setForeground(Color.WHITE);
        btnNewAcc.setFocusPainted(false);
        btnNewAcc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNewAcc.addActionListener(e -> new AddAccountDialog(parentFrame).setVisible(true));

        btnGroup.add(btnYieldTest);
        btnGroup.add(btnNewAcc);
        topPanel.add(btnGroup, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center: Grid of Account Cards
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshAccounts() {
        SwingUtilities.invokeLater(() -> {
            try {
                User user = AuthenticationService.getInstance().getCurrentUser();
                Long userId = user != null ? user.getId() : 1L;
                List<Account> accounts = financeService.getAccounts(userId);

                cardsContainer.removeAll();

                for (Account acc : accounts) {
                    cardsContainer.add(createAccountCard(acc));
                    cardsContainer.add(Box.createVerticalStrut(12));
                }

                cardsContainer.revalidate();
                cardsContainer.repaint();
            } catch (Exception e) {
                System.err.println("[AccountsPanel] Error loading accounts: " + e.getMessage());
            }
        });
    }

    private CardPanel createAccountCard(Account acc) {
        CardPanel card = new CardPanel(new BorderLayout(15, 0), 12, Color.WHITE, new Color(226, 232, 240));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Left: Account Details
        JPanel left = new JPanel(new GridLayout(3, 1, 0, 4));
        left.setOpaque(false);

        JLabel lblName = new JLabel(acc.getAccountName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(new Color(30, 41, 59));

        JLabel lblNumber = new JLabel("Number: " + acc.getAccountNumber() + "  |  Currency: " + acc.getCurrency());
        lblNumber.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNumber.setForeground(new Color(100, 116, 139));

        JLabel lblSpecialized = new JLabel(acc.getAccountSummary());
        lblSpecialized.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSpecialized.setForeground(new Color(79, 70, 229));

        left.add(lblName);
        left.add(lblNumber);
        left.add(lblSpecialized);
        card.add(left, BorderLayout.CENTER);

        // Right: Balance & Status Pill
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 4));
        right.setOpaque(false);

        JLabel lblBalance = new JLabel(CurrencyFormatter.formatUSD(acc.getBalance()), JLabel.RIGHT);
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBalance.setForeground(new Color(15, 23, 42));

        JLabel lblBadge = new JLabel("● " + acc.getAccountType() + " (" + acc.getStatus() + ")", JLabel.RIGHT);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        if (acc instanceof SavingsAccount) {
            lblBadge.setForeground(new Color(34, 197, 94));
        } else if (acc instanceof CheckingAccount) {
            lblBadge.setForeground(new Color(59, 130, 246));
        } else {
            lblBadge.setForeground(new Color(168, 85, 247));
        }

        right.add(lblBalance);
        right.add(lblBadge);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private void runPolymorphicYieldSimulation() {
        try {
            User user = AuthenticationService.getInstance().getCurrentUser();
            Long userId = user != null ? user.getId() : 1L;
            List<Account> accounts = financeService.getAccounts(userId);

            StringBuilder sb = new StringBuilder();
            sb.append("=== Polymorphic Method Invocation: calculateMonthlyYieldOrFee() ===\n\n");

            for (Account acc : accounts) {
                // Polymorphic method call
                BigDecimal projection = acc.calculateMonthlyYieldOrFee();
                sb.append("Account: ").append(acc.getAccountName()).append(" [").append(acc.getAccountType()).append("]\n");
                sb.append("Current Balance: ").append(CurrencyFormatter.formatUSD(acc.getBalance())).append("\n");
                sb.append("Polymorphic Monthly Impact: ");
                if (projection.compareTo(BigDecimal.ZERO) >= 0) {
                    sb.append("+").append(CurrencyFormatter.formatUSD(projection)).append(" (Accrued Yield/Interest)\n");
                } else {
                    sb.append(CurrencyFormatter.formatUSD(projection)).append(" (Maintenance / Overdraft Fee)\n");
                }
                sb.append("----------------------------------------------------------------------\n");
            }

            sb.append("\nNote: Each subclass (SavingsAccount, CheckingAccount, InvestmentAccount) overrides\n");
            sb.append("calculateMonthlyYieldOrFee() differently, demonstrating runtime dynamic dispatch (Polymorphism)!");

            JOptionPane.showMessageDialog(this, sb.toString(), "Polymorphic Simulation Results", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Simulation error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onDataChanged(String eventType, Object payload) {
        refreshAccounts();
    }
}
