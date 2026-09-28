package com.finvantage.ui.panels;

import com.finvantage.event.FinanceEventListener;
import com.finvantage.event.FinanceEventManager;
import com.finvantage.model.AuditLog;
import com.finvantage.service.FinanceService;
import com.finvantage.ui.components.CardPanel;
import com.finvantage.ui.components.CustomTable;
import com.finvantage.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.util.List;

/**
 * AuditLogPanel displays system event logs, security entries, and high-value transactions
 * captured automatically via Oracle triggers or backend transaction monitors.
 */
public class AuditLogPanel extends JPanel implements FinanceEventListener {

    private final JFrame parentFrame;
    private final FinanceService financeService = FinanceService.getInstance();
    private DefaultTableModel tableModel;

    public AuditLogPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setBackground(new Color(248, 250, 252));

        initComponents();
        FinanceEventManager.getInstance().addListener(this);
        refreshAuditLogs();
    }

    private void initComponents() {
        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Security & Transaction Audit Trail");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Audit log records populated via Oracle Database Triggers and ACID commit hooks");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        titleGroup.add(lblTitle);
        titleGroup.add(lblSubtitle);
        topPanel.add(titleGroup, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Refresh Audit Logs");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> refreshAuditLogs());
        topPanel.add(btnRefresh, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table Card
        CardPanel tableCard = new CardPanel(new BorderLayout(), 12, Color.WHITE, new Color(226, 232, 240));
        tableCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Log ID", "Action Type", "Target Entity", "Record #", "Timestamp", "Audit Details"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        CustomTable table = new CustomTable(tableModel);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        tableCard.add(sp, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshAuditLogs() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<AuditLog> logs = financeService.getRecentAuditLogs(50);
                tableModel.setRowCount(0);
                for (AuditLog log : logs) {
                    tableModel.addRow(new Object[]{
                        log.getId() != null ? "#" + log.getId() : "N/A",
                        log.getActionType(),
                        log.getTableName(),
                        log.getRecordId() != null ? log.getRecordId() : "-",
                        log.getTimestamp() != null ? log.getTimestamp().format(ValidationUtil.DATE_TIME_FORMATTER) : "N/A",
                        log.getDetails()
                    });
                }
            } catch (Exception e) {
                System.err.println("[AuditLogPanel] Error fetching audit logs: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDataChanged(String eventType, Object payload) {
        refreshAuditLogs();
    }
}
