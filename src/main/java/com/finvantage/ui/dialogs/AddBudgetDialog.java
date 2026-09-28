package com.finvantage.ui.dialogs;

import com.finvantage.model.Budget;
import com.finvantage.model.Category;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.FinanceService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modal dialog for establishing or adjusting monthly category spending limits.
 */
public class AddBudgetDialog extends JDialog {

    private final JComboBox<CategoryItem> cmbCategory = new JComboBox<>();
    private final JTextField txtMonthYear = new JTextField(15);
    private final JTextField txtLimit = new JTextField(15);
    private final JTextField txtThreshold = new JTextField("80.00", 15);

    private final FinanceService financeService = FinanceService.getInstance();
    private final Long userId;

    public AddBudgetDialog(JFrame parent) {
        super(parent, "Set Category Spending Budget", true);
        User user = AuthenticationService.getInstance().getCurrentUser();
        this.userId = user != null ? user.getId() : 1L;

        initComponents();
        loadCategories();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setResizable(false);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(254, 243, 199));
        JLabel lblHeader = new JLabel("Define Monthly Category Budget");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(new Color(146, 64, 14));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMonthYear.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));

        int row = 0;
        addFormRow(form, gbc, row++, "Expense Category:", cmbCategory);
        addFormRow(form, gbc, row++, "Billing Period (YYYY-MM):", txtMonthYear);
        addFormRow(form, gbc, row++, "Budget Cap ($ USD):", txtLimit);
        addFormRow(form, gbc, row++, "Warning Alert Level (%):", txtThreshold);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(new Color(248, 250, 252));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = new JButton("Set Budget");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmit.setBackground(new Color(217, 119, 6));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setPreferredSize(new Dimension(130, 32));
        btnSubmit.addActionListener(e -> handleSaveBudget());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Object component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add((java.awt.Component) component, gbc);
    }

    private void loadCategories() {
        try {
            List<Category> categories = financeService.getCategories(userId);
            cmbCategory.removeAllItems();
            for (Category cat : categories) {
                if (cat.getType() == Category.Type.EXPENSE) {
                    cmbCategory.addItem(new CategoryItem(cat.getId(), cat.getName()));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveBudget() {
        CategoryItem item = (CategoryItem) cmbCategory.getSelectedItem();
        String period = txtMonthYear.getText().trim();
        String limitText = txtLimit.getText().trim();
        String threshText = txtThreshold.getText().trim();

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an expense category.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal limit;
        BigDecimal thresh;
        try {
            limit = new BigDecimal(limitText);
            thresh = new BigDecimal(threshText);
            if (limit.compareTo(BigDecimal.ZERO) <= 0 || thresh.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter positive numeric values for limit and threshold.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Budget budget = new Budget(null, userId, item.id, period, limit, thresh);
            financeService.createBudget(budget);
            JOptionPane.showMessageDialog(this, "Monthly budget successfully established!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save budget: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class CategoryItem {
        final Long id;
        final String label;

        CategoryItem(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
