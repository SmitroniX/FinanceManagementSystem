package com.finvantage.ui.dialogs;

import com.finvantage.model.Account;
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
import java.util.List;

/**
 * Modal dialog for recording an Income or Expense transaction.
 */
public class AddTransactionDialog extends JDialog {

    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"EXPENSE", "INCOME"});
    private final JComboBox<AccountItem> cmbAccount = new JComboBox<>();
    private final JComboBox<CategoryItem> cmbCategory = new JComboBox<>();
    private final JTextField txtAmount = new JTextField(15);
    private final JTextField txtDescription = new JTextField(20);

    private final FinanceService financeService = FinanceService.getInstance();
    private final Long userId;

    public AddTransactionDialog(JFrame parent) {
        super(parent, "Record Financial Transaction", true);
        User user = AuthenticationService.getInstance().getCurrentUser();
        this.userId = user != null ? user.getId() : 1L;

        initComponents();
        loadAccountsAndCategories();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setResizable(false);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(241, 245, 249));
        JLabel lblHeader = new JLabel("Record New Income or Expense");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(new Color(30, 41, 59));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // Form Body
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(form, gbc, row++, "Transaction Type:", cmbType);
        addFormRow(form, gbc, row++, "Select Account:", cmbAccount);
        addFormRow(form, gbc, row++, "Category:", cmbCategory);
        addFormRow(form, gbc, row++, "Amount ($ USD):", txtAmount);
        addFormRow(form, gbc, row++, "Description / Note:", txtDescription);

        add(form, BorderLayout.CENTER);

        // Button Bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(new Color(248, 250, 252));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = new JButton("Save Transaction");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmit.setBackground(new Color(37, 99, 235));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setPreferredSize(new Dimension(140, 32));
        btnSubmit.addActionListener(e -> handleSubmit());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Object component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(71, 85, 105));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add((java.awt.Component) component, gbc);
    }

    private void loadAccountsAndCategories() {
        try {
            List<Account> accounts = financeService.getAccounts(userId);
            cmbAccount.removeAllItems();
            for (Account acc : accounts) {
                cmbAccount.addItem(new AccountItem(acc.getId(), acc.getAccountName() + " ($" + acc.getBalance() + ")"));
            }

            List<Category> categories = financeService.getCategories(userId);
            cmbCategory.removeAllItems();
            for (Category cat : categories) {
                cmbCategory.addItem(new CategoryItem(cat.getId(), cat.getName(), cat.getType().name()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading form options: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSubmit() {
        String type = (String) cmbType.getSelectedItem();
        AccountItem accItem = (AccountItem) cmbAccount.getSelectedItem();
        CategoryItem catItem = (CategoryItem) cmbCategory.getSelectedItem();
        String amtText = txtAmount.getText().trim();
        String desc = txtDescription.getText().trim();

        if (accItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an account.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive numeric amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Long catId = catItem != null ? catItem.id : null;
            if ("INCOME".equalsIgnoreCase(type)) {
                financeService.recordIncome(accItem.id, catId, amount, desc);
            } else {
                financeService.recordExpense(accItem.id, catId, amount, desc);
            }
            JOptionPane.showMessageDialog(this, "Transaction successfully recorded!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to record transaction:\n" + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class AccountItem {
        final Long id;
        final String label;

        AccountItem(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class CategoryItem {
        final Long id;
        final String label;
        final String type;

        CategoryItem(Long id, String label, String type) {
            this.id = id;
            this.label = label;
            this.type = type;
        }

        @Override
        public String toString() {
            return label + " (" + type + ")";
        }
    }
}
