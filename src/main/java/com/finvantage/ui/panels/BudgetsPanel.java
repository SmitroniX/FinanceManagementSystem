package com.finvantage.ui.panels;

import com.finvantage.event.FinanceEventListener;
import com.finvantage.event.FinanceEventManager;
import com.finvantage.model.Budget;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.FinanceService;
import com.finvantage.ui.components.CardPanel;
import com.finvantage.ui.dialogs.AddBudgetDialog;
import com.finvantage.util.CurrencyFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BudgetsPanel visualizes category spending caps, utilization percentages,
 * and thresholds using color-coded progress bars and status indicators.
 */
public class BudgetsPanel extends JPanel implements FinanceEventListener {

    private final JFrame parentFrame;
    private final FinanceService financeService = FinanceService.getInstance();
    private final JPanel budgetListContainer = new JPanel();
    private final String currentMonthYear = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

    public BudgetsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setBackground(new Color(248, 250, 252));

        initComponents();
        FinanceEventManager.getInstance().addListener(this);
        refreshBudgets();
    }

    private void initComponents() {
        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Category Budgets & Expense Controls");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Active monitoring for billing period: " + currentMonthYear);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        titleGroup.add(lblTitle);
        titleGroup.add(lblSubtitle);
        topPanel.add(titleGroup, BorderLayout.WEST);

        JButton btnNewBudget = new JButton("+ Set Category Budget");
        btnNewBudget.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNewBudget.setBackground(new Color(217, 119, 6));
        btnNewBudget.setForeground(Color.WHITE);
        btnNewBudget.setFocusPainted(false);
        btnNewBudget.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNewBudget.addActionListener(e -> new AddBudgetDialog(parentFrame).setVisible(true));
        topPanel.add(btnNewBudget, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center: List of Budgets
        budgetListContainer.setLayout(new BoxLayout(budgetListContainer, BoxLayout.Y_AXIS));
        budgetListContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(budgetListContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshBudgets() {
        SwingUtilities.invokeLater(() -> {
            try {
                User user = AuthenticationService.getInstance().getCurrentUser();
                Long userId = user != null ? user.getId() : 1L;
                List<Budget> budgets = financeService.getBudgets(userId, currentMonthYear);

                budgetListContainer.removeAll();

                if (budgets.isEmpty()) {
                    JLabel lblEmpty = new JLabel("No budgets established for " + currentMonthYear + ". Click '+ Set Category Budget' to create one.");
                    lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    lblEmpty.setForeground(new Color(148, 163, 184));
                    budgetListContainer.add(lblEmpty);
                } else {
                    for (Budget b : budgets) {
                        budgetListContainer.add(createBudgetCard(b));
                        budgetListContainer.add(Box.createVerticalStrut(12));
                    }
                }

                budgetListContainer.revalidate();
                budgetListContainer.repaint();
            } catch (Exception e) {
                System.err.println("[BudgetsPanel] Error refreshing budgets: " + e.getMessage());
            }
        });
    }

    private CardPanel createBudgetCard(Budget b) {
        CardPanel card = new CardPanel(new BorderLayout(15, 10), 12, Color.WHITE, new Color(226, 232, 240));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Header Row: Category Name & Status Badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblName = new JLabel(b.getCategoryName() != null ? b.getCategoryName() : "General Expense");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(new Color(30, 41, 59));

        JLabel lblStatus = new JLabel(b.getStatusBadge());
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));

        if (b.isExceeded()) {
            lblStatus.setForeground(new Color(220, 38, 38));
        } else if (b.isWarning()) {
            lblStatus.setForeground(new Color(217, 119, 6));
        } else {
            lblStatus.setForeground(new Color(22, 163, 74));
        }

        top.add(lblName, BorderLayout.WEST);
        top.add(lblStatus, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Center: Progress Bar
        JProgressBar pb = new JProgressBar(0, 100);
        int pct = (int) Math.min(100, Math.round(b.getUtilizationPercentage()));
        pb.setValue(pct);
        pb.setStringPainted(false);
        pb.setPreferredSize(new Dimension(0, 8));
        pb.setBackground(new Color(241, 245, 249));

        if (b.isExceeded()) {
            pb.setForeground(new Color(239, 68, 68));
        } else if (b.isWarning()) {
            pb.setForeground(new Color(245, 158, 11));
        } else {
            pb.setForeground(new Color(34, 197, 94));
        }

        card.add(pb, BorderLayout.CENTER);

        // Footer: Spent, Budget Cap, and Remaining
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel lblSpent = new JLabel("Spent: " + CurrencyFormatter.formatUSD(b.getActualSpent()) + 
                                     "  /  Budget Cap: " + CurrencyFormatter.formatUSD(b.getBudgetLimit()));
        lblSpent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSpent.setForeground(new Color(100, 116, 139));

        JLabel lblRemaining = new JLabel("Remaining: " + CurrencyFormatter.formatUSD(b.getRemainingAmount()));
        lblRemaining.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRemaining.setForeground(new Color(71, 85, 105));

        bottom.add(lblSpent, BorderLayout.WEST);
        bottom.add(lblRemaining, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    @Override
    public void onDataChanged(String eventType, Object payload) {
        refreshBudgets();
    }
}
