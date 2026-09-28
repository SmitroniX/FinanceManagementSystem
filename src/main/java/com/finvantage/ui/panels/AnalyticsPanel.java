package com.finvantage.ui.panels;

import com.finvantage.event.FinanceEventListener;
import com.finvantage.event.FinanceEventManager;
import com.finvantage.model.FinancialSummary;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.FinanceService;
import com.finvantage.service.ReportService;
import com.finvantage.service.ReportService.CategoryMetric;
import com.finvantage.ui.components.CardPanel;
import com.finvantage.ui.components.CustomTable;
import com.finvantage.util.CurrencyFormatter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.Map;

/**
 * AnalyticsPanel provides financial intelligence, category ranking,
 * and highlights relational DBMS analytical view concepts.
 */
public class AnalyticsPanel extends JPanel implements FinanceEventListener {

    private final JFrame parentFrame;
    private final FinanceService financeService = FinanceService.getInstance();
    private final ReportService reportService = ReportService.getInstance();

    private DefaultTableModel tableModel;
    private JLabel lblSavingsRate;
    private JLabel lblBurnRate;
    private JLabel lblTotalOutflow;

    public AnalyticsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setBackground(new Color(248, 250, 252));

        initComponents();
        FinanceEventManager.getInstance().addListener(this);
        refreshAnalytics();
    }

    private void initComponents() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Financial Analytics & Relational Views");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Aggregated category allocations and DBMS SQL View definitions");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        titleGroup.add(lblTitle);
        titleGroup.add(lblSubtitle);
        topPanel.add(titleGroup, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Center Split: KPI Overview & Table
        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setOpaque(false);

        // Metrics Banner
        CardPanel banner = new CardPanel(new GridLayout(1, 3, 15, 0), 12, Color.WHITE, new Color(226, 232, 240));
        banner.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        lblSavingsRate = createMetricLabel("Net Savings Rate", "0.0%", new Color(34, 197, 94));
        lblBurnRate = createMetricLabel("Outflow / Inflow Ratio", "0.0%", new Color(239, 68, 68));
        lblTotalOutflow = createMetricLabel("Total Monthly Outflow", "$0.00", new Color(79, 70, 229));

        banner.add(lblSavingsRate.getParent());
        banner.add(lblBurnRate.getParent());
        banner.add(lblTotalOutflow.getParent());
        center.add(banner, BorderLayout.NORTH);

        // Table Card
        CardPanel tableCard = new CardPanel(new BorderLayout(), 12, Color.WHITE, new Color(226, 232, 240));
        tableCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTableTitle = new JLabel("Category Expense Ranking (Derived via Analytical Group By & Sum)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTableTitle.setForeground(new Color(30, 41, 59));
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Expense Category", "Total Outflow ($ USD)", "Share of Overall Spending (%)", "Budget Advisory"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        CustomTable table = new CustomTable(tableModel);
        table.getColumnModel().getColumn(1).setCellRenderer(CustomTable.getAmountRenderer());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        tableCard.add(sp, BorderLayout.CENTER);

        center.add(tableCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JLabel createMetricLabel(String title, String initialVal, Color accent) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setOpaque(false);
        JLabel t = new JLabel(title.toUpperCase());
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(new Color(100, 116, 139));

        JLabel v = new JLabel(initialVal);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(accent);

        p.add(t);
        p.add(v);
        return v;
    }

    public void refreshAnalytics() {
        SwingUtilities.invokeLater(() -> {
            try {
                User user = AuthenticationService.getInstance().getCurrentUser();
                Long userId = user != null ? user.getId() : 1L;

                FinancialSummary summary = financeService.getFinancialSummary(userId);
                lblSavingsRate.setText(String.format("%.1f%%", summary.getSavingsRate()));
                
                double burnRate = 0.0;
                if (summary.getMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
                    burnRate = summary.getMonthlyExpense().doubleValue() / summary.getMonthlyIncome().doubleValue() * 100.0;
                }
                lblBurnRate.setText(String.format("%.1f%%", burnRate));
                lblTotalOutflow.setText(CurrencyFormatter.formatUSD(summary.getMonthlyExpense()));

                Map<String, CategoryMetric> breakdown = reportService.getCategoryExpenseBreakdown(userId);
                tableModel.setRowCount(0);
                for (CategoryMetric cm : breakdown.values()) {
                    String advisory = cm.getPercentage() > 40.0 ? "High Allocation (>40%)" : "Optimal (<=40%)";
                    tableModel.addRow(new Object[]{
                        cm.getCategoryName(),
                        "-" + CurrencyFormatter.formatUSD(cm.getTotalAmount()),
                        String.format("%.2f%%", cm.getPercentage()),
                        advisory
                    });
                }
            } catch (Exception e) {
                System.err.println("[AnalyticsPanel] Error refreshing analytics: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDataChanged(String eventType, Object payload) {
        refreshAnalytics();
    }
}
