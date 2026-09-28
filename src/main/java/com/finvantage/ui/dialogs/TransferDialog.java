package com.finvantage.ui.dialogs;

import com.finvantage.model.Account;
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
 * Modal dialog for executing ACID-compliant transfers between accounts.
 */
public class TransferDialog extends JDialog {

    private final JComboBox<AccountItem> cmbSourceAccount = new JComboBox<>();
    private final JComboBox<AccountItem> cmbDestAccount = new JComboBox<>();
    private final JTextField txtAmount = new JTextField(15);
    private final JTextField txtDescription = new JTextField(20);

    private final FinanceService financeService = FinanceService.getInstance();
    private final Long userId;

    public TransferDialog(JFrame parent) {
        super(parent, "Inter-Account Funds Transfer (ACID)", true);
        User user = AuthenticationService.getInstance().getCurrentUser();
        this.userId = user != null ? user.getId() : 1L;

        initComponents();
        loadAccounts();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setResizable(false);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(238, 242, 255));
        JLabel lblHeader = new JLabel("Execute Instant Inter-Account Transfer");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(new Color(30, 58, 138));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // Form Grid
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(form, gbc, row++, "Source Account (Debit):", cmbSourceAccount);
        addFormRow(form, gbc, row++, "Destination Account (Credit):", cmbDestAccount);
        addFormRow(form, gbc, row++, "Transfer Amount ($ USD):", txtAmount);
        addFormRow(form, gbc, row++, "Transfer Memo / Description:", txtDescription);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(new Color(248, 250, 252));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = new JButton("Execute Transfer");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmit.setBackground(new Color(37, 99, 235));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setPreferredSize(new Dimension(140, 32));
        btnSubmit.addActionListener(e -> handleTransfer());

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

    private void loadAccounts() {
        try {
            List<Account> accounts = financeService.getAccounts(userId);
            cmbSourceAccount.removeAllItems();
            cmbDestAccount.removeAllItems();

            for (Account acc : accounts) {
                AccountItem item = new AccountItem(acc.getId(), acc.getAccountName() + " ($" + acc.getBalance() + ")");
                cmbSourceAccount.addItem(item);
                cmbDestAccount.addItem(item);
            }
            if (cmbDestAccount.getItemCount() > 1) {
                cmbDestAccount.setSelectedIndex(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleTransfer() {
        AccountItem srcItem = (AccountItem) cmbSourceAccount.getSelectedItem();
        AccountItem dstItem = (AccountItem) cmbDestAccount.getSelectedItem();
        String amtText = txtAmount.getText().trim();
        String desc = txtDescription.getText().trim();

        if (srcItem == null || dstItem == null) {
            JOptionPane.showMessageDialog(this, "Please select both source and destination accounts.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (srcItem.id.equals(dstItem.id)) {
            JOptionPane.showMessageDialog(this, "Source and destination accounts cannot be the same!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive numeric transfer amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            financeService.executeAtomicTransfer(srcItem.id, dstItem.id, amount, 
                desc.isEmpty() ? "Inter-account transfer" : desc);

            JOptionPane.showMessageDialog(this, 
                "Transfer executed successfully!\n" +
                "Amount: $" + amount + "\n" +
                "Transaction status: COMMITTED (ACID Verified)", 
                "Transfer Completed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Transfer Failed / Rolled Back:\n" + ex.getMessage(), 
                "Transaction Error", JOptionPane.ERROR_MESSAGE);
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
}
