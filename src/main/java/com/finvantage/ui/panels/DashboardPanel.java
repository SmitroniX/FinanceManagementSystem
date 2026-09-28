package com.finvantage.ui.panels;

import com.finvantage.event.FinanceEventListener;
import com.finvantage.event.FinanceEventManager;
import com.finvantage.model.FinancialSummary;
import com.finvantage.model.Transaction;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.FinanceService;
import com.finvantage.service.ReportService;
import com.finvantage.ui.components.CardPanel;
import com.finvantage.ui.components.CashFlowBarChart;
import com.finvantage.ui.components.CategoryDonutChart;
import com.finvantage.ui.components.CustomTable;
import com.finvantage.ui.components.StatCard;
import com.finvantage.ui.dialogs.AddAccountDialog;
import com.finvantage.ui.dialogs.AddTransactionDialog;
import com.finvantage.ui.dialogs.TransferDialog;
import com.finvantage.util.CurrencyFormatter;
import com.finvantage.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * Main financial command dashboard providing KPI stat cards, custom visual charts,
 * quick ledger actions, and recent transaction feeds.
 * Implements the Observer Pattern via FinanceEventListener.
 */
public class DashboardPanel extends JPanel implements FinanceEventListener {

    private final JFrame parentFrame;
    private final FinanceService financeService = FinanceService.getInstance();
    private final ReportService reportService = ReportService.getInstance();

    private StatCard cardNetWorth;
    private StatCard cardIncome;
    private StatCard cardExpense;
    private StatCard cardSavings;

    private CashFlowBarChart barChart;
    private CategoryDonutChart donutChart;
    private DefaultTableModel tableModel;

    public DashboardPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setBackground(new Color(248, 250, 252));

        initComponents();
        FinanceEventManager.getInstance().addListener(this);
        refreshData();
    }

    private void initComponents() {
        // Top Container: Header & Action Bar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Financial Overview & Intelligence");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Real-time double-entry ledger & liquidity positions");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);
        textGroup.add(lblTitle);
        textGroup.add(lblSubtitle);
        titlePanel.add(textGroup, BorderLayout.WEST);

        // Quick Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnIncome = createActionButton("+ Record Income", new Color(22, 163, 74));
        btnIncome.addActionListener(e -> new AddTransactionDialog(parentFrame).setVisible(true));

        JButton btnExpense = createActionButton("- Record Expense", new Color(220, 38, 38));
        btnExpense.addActionListener(e -> new AddTransactionDialog(parentFrame).setVisible(true));

        JButton btnTransfer = createActionButton("⇆ Inter-Account Transfer", new Color(37, 99, 235));
        btnTransfer.addActionListener(e -> new TransferDialog(parentFrame).setVisible(true));

        JButton btnAccount = createActionButton("+ New Account", new Color(79, 70, 229));
        btnAccount.addActionListener(e -> new AddAccountDialog(parentFrame).setVisible(true));

        actionPanel.add(btnIncome);
        actionPanel.add(btnExpense);
        actionPanel.add(btnTransfer);
        actionPanel.add(btnAccount);
        titlePanel.add(actionPanel, BorderLayout.EAST);

        topContainer.add(titlePanel);
        topContainer.add(Box.createVerticalStrut(18));

        // KPI Stat Cards Row
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiGrid.setOpaque(false);

        cardNetWorth = new StatCard("Total Net Worth", "$0.00", "0 Accounts", new Color(59, 130, 246));
        cardIncome = new StatCard("Monthly Inflow", "$0.00", "Current Period", new Color(34, 197, 94));
        cardExpense = new StatCard("Monthly Outflow", "$0.00", "Current Period", new Color(239, 68, 68));
        cardSavings = new StatCard("Net Savings Rate", "0.0%", "$0.00 Net Savings", new Color(168, 85, 247));

        kpiGrid.add(cardNetWorth);
        kpiGrid.add(cardIncome);
        kpiGrid.add(cardExpense);
        kpiGrid.add(cardSavings);

        topContainer.add(kpiGrid);
        add(topContainer, BorderLayout.NORTH);

        // Center: Dual Charts & Recent Transactions
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        chartsRow.setOpaque(false);

        CardPanel pnlBar = new CardPanel(new BorderLayout(), 12, Color.WHITE, new Color(226, 232, 240));
        pnlBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        barChart = new CashFlowBarChart();
        pnlBar.add(barChart, BorderLayout.CENTER);

        CardPanel pnlDonut = new CardPanel(new BorderLayout(), 12, Color.WHITE, new Color(226, 232, 240));
        pnlDonut.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        donutChart = new CategoryDonutChart();
        pnlDonut.add(donutChart, BorderLayout.CENTER);

        chartsRow.add(pnlBar);
        chartsRow.add(pnlDonut);
        centerPanel.add(chartsRow, BorderLayout.NORTH);

        // Recent Transactions Section
        CardPanel tableCard = new CardPanel(new BorderLayout(), 12, Color.WHITE, new Color(226, 232, 240));
        tableCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblRecent = new JLabel("Recent Financial Activity");
        lblRecent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRecent.setForeground(new Color(30, 41, 59));
        tableHeader.add(lblRecent, BorderLayout.WEST);

        String[] columns = {"Ref #", "Date & Time", "Type", "Category", "Description", "Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        CustomTable table = new CustomTable(tableModel);
        table.getColumnModel().getColumn(2).setCellRenderer(CustomTable.getTypeBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(CustomTable.getAmountRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 12, 34));
        return btn;
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                User user = AuthenticationService.getInstance().getCurrentUser();
                Long userId = user != null ? user.getId() : 1L;

                // 1. Refresh KPI cards
                FinancialSummary summary = financeService.getFinancialSummary(userId);
                cardNetWorth.updateData(CurrencyFormatter.formatUSD(summary.getTotalNetWorth()), summary.getActiveAccountsCount() + " Active Accounts");
                cardIncome.updateData(CurrencyFormatter.formatUSD(summary.getMonthlyIncome()), "Current Period Inflow");
                cardExpense.updateData(CurrencyFormatter.formatUSD(summary.getMonthlyExpense()), "Current Period Outflow");
                cardSavings.updateData(String.format("%.1f%%", summary.getSavingsRate()), CurrencyFormatter.formatUSD(summary.getNetSavings()) + " Net Saved");

                // 2. Refresh Charts
                barChart.updateMetrics(summary.getMonthlyIncome(), summary.getMonthlyExpense());
                donutChart.updateData(reportService.getCategoryExpenseBreakdown(userId));

                // 3. Refresh Transactions Table
                List<Transaction> txs = financeService.getRecentTransactions(userId, 8);
                tableModel.setRowCount(0);
                for (Transaction tx : txs) {
                    tableModel.addRow(new Object[]{
                        tx.getTransactionRef(),
                        tx.getTransactionDate().format(ValidationUtil.DATE_TIME_FORMATTER),
                        tx.getTransactionType(),
                        tx.getCategoryName() != null ? tx.getCategoryName() : "Transfer / Misc",
                        tx.getDescription(),
                        CurrencyFormatter.formatSignedUSD(tx.getAmount(), tx.getTransactionType()),
                        tx.getStatus()
                    });
                }
            } catch (Exception e) {
                System.err.println("[DashboardPanel] Error refreshing dashboard: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDataChanged(String eventType, Object payload) {
        refreshData();
    }
}
