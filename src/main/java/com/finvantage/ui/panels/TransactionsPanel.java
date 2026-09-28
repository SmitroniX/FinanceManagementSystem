package com.finvantage.ui.panels;

import com.finvantage.event.FinanceEventListener;
import com.finvantage.event.FinanceEventManager;
import com.finvantage.model.Transaction;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.FinanceService;
import com.finvantage.service.ReportService;
import com.finvantage.ui.components.CardPanel;
import com.finvantage.ui.components.CustomTable;
import com.finvantage.ui.dialogs.AddTransactionDialog;
import com.finvantage.ui.dialogs.TransferDialog;
import com.finvantage.util.CurrencyFormatter;
import com.finvantage.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Ledger Panel providing real-time search, category filtering,
 * ledger pagination, and CSV data export capabilities.
 */
public class TransactionsPanel extends JPanel implements FinanceEventListener {

    private final JFrame parentFrame;
    private final FinanceService financeService = FinanceService.getInstance();
    private final ReportService reportService = ReportService.getInstance();

    private final JTextField txtSearch = new JTextField(15);
    private final JComboBox<String> cmbTypeFilter = new JComboBox<>(new String[]{"ALL TYPES", "INCOME", "EXPENSE", "TRANSFER"});
    private DefaultTableModel tableModel;
    private List<Transaction> allLoadedTransactions = new ArrayList<>();
    private final JLabel lblCount = new JLabel("0 Total Transactions");

    public TransactionsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        setBackground(new Color(248, 250, 252));

        initComponents();
        FinanceEventManager.getInstance().addListener(this);
        refreshTransactions();
    }

    private void initComponents() {
        // Top Section: Title & Controls
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Financial Ledger & Transactions");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Searchable double-entry ledger with immutable audit integrity");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        titleGroup.add(lblTitle);
        titleGroup.add(lblSubtitle);
        titlePanel.add(titleGroup, BorderLayout.WEST);

        // Action Buttons
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtons.setOpaque(false);

        JButton btnIncome = createBtn("+ Record Income", new Color(22, 163, 74));
        btnIncome.addActionListener(e -> new AddTransactionDialog(parentFrame).setVisible(true));

        JButton btnExpense = createBtn("- Record Expense", new Color(220, 38, 38));
        btnExpense.addActionListener(e -> new AddTransactionDialog(parentFrame).setVisible(true));

        JButton btnTransfer = createBtn("⇆ Transfer", new Color(37, 99, 235));
        btnTransfer.addActionListener(e -> new TransferDialog(parentFrame).setVisible(true));

        JButton btnExport = createBtn("📥 Export CSV", new Color(71, 85, 105));
        btnExport.addActionListener(e -> handleExportCSV());

        actionButtons.add(btnIncome);
        actionButtons.add(btnExpense);
        actionButtons.add(btnTransfer);
        actionButtons.add(btnExport);
        titlePanel.add(actionButtons, BorderLayout.EAST);

        topContainer.add(titlePanel);
        topContainer.add(Box.createVerticalStrut(15));

        // Filter Bar
        CardPanel filterCard = new CardPanel(new BorderLayout(15, 0), 10, Color.WHITE, new Color(226, 232, 240));
        filterCard.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel filterInputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterInputs.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterInputs.add(lblSearch);
        filterInputs.add(txtSearch);

        JLabel lblFilter = new JLabel("Filter Type:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterInputs.add(lblFilter);
        filterInputs.add(cmbTypeFilter);

        filterCard.add(filterInputs, BorderLayout.WEST);

        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCount.setForeground(new Color(100, 116, 139));
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        countPanel.setOpaque(false);
        countPanel.add(lblCount);
        filterCard.add(countPanel, BorderLayout.EAST);

        topContainer.add(filterCard);
        add(topContainer, BorderLayout.NORTH);

        // Center: Custom JTable
        CardPanel tableCard = new CardPanel(new BorderLayout(), 12, Color.WHITE, new Color(226, 232, 240));
        tableCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] columns = {"Reference Code", "Timestamp", "Type", "Classification", "Description", "Amount ($ USD)", "Status"};
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
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // Search & Filter Listeners
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        cmbTypeFilter.addActionListener(e -> applyFilters());
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 10, 32));
        return btn;
    }

    public void refreshTransactions() {
        SwingUtilities.invokeLater(() -> {
            try {
                User user = AuthenticationService.getInstance().getCurrentUser();
                Long userId = user != null ? user.getId() : 1L;
                allLoadedTransactions = financeService.getRecentTransactions(userId, 500);
                applyFilters();
            } catch (Exception e) {
                System.err.println("[TransactionsPanel] Error refreshing transactions: " + e.getMessage());
            }
        });
    }

    private void applyFilters() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedType = (String) cmbTypeFilter.getSelectedItem();

        tableModel.setRowCount(0);
        int matchingCount = 0;

        for (Transaction tx : allLoadedTransactions) {
            boolean typeMatches = "ALL TYPES".equals(selectedType) || tx.getTransactionType().equalsIgnoreCase(selectedType);
            boolean searchMatches = keyword.isEmpty() ||
                tx.getTransactionRef().toLowerCase().contains(keyword) ||
                (tx.getDescription() != null && tx.getDescription().toLowerCase().contains(keyword)) ||
                (tx.getCategoryName() != null && tx.getCategoryName().toLowerCase().contains(keyword));

            if (typeMatches && searchMatches) {
                tableModel.addRow(new Object[]{
                    tx.getTransactionRef(),
                    tx.getTransactionDate().format(ValidationUtil.DATE_TIME_FORMATTER),
                    tx.getTransactionType(),
                    tx.getCategoryName() != null ? tx.getCategoryName() : "Transfer / Misc",
                    tx.getDescription(),
                    CurrencyFormatter.formatSignedUSD(tx.getAmount(), tx.getTransactionType()),
                    tx.getStatus()
                });
                matchingCount++;
            }
        }
        lblCount.setText(matchingCount + " of " + allLoadedTransactions.size() + " Transactions Shown");
    }

    private void handleExportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("FinVantage_Ledger_Export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                User user = AuthenticationService.getInstance().getCurrentUser();
                Long userId = user != null ? user.getId() : 1L;
                reportService.exportTransactionsToCSV(userId, target);
                JOptionPane.showMessageDialog(this, 
                    "Transactions successfully exported to:\n" + target.getAbsolutePath(), 
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onDataChanged(String eventType, Object payload) {
        refreshTransactions();
    }
}
