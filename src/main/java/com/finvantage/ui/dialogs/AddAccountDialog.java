package com.finvantage.ui.dialogs;

import com.finvantage.factory.AccountFactory;
import com.finvantage.model.Account;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.FinanceService;
import com.finvantage.util.ValidationUtil;

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

/**
 * Modal dialog for opening new accounts (Savings, Checking, or Investment).
 * Dynamically adjusts input fields based on polymorphic account types.
 */
public class AddAccountDialog extends JDialog {

    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"SAVINGS", "CHECKING", "INVESTMENT"});
    private final JTextField txtAccountName = new JTextField(18);
    private final JTextField txtInitialBalance = new JTextField("0.00", 18);
    
    // Type specific fields
    private final JLabel lblParam1 = new JLabel("Annual Interest Rate (%):");
    private final JTextField txtParam1 = new JTextField("4.50", 18);

    private final JLabel lblParam2 = new JLabel("Minimum Balance ($):");
    private final JTextField txtParam2 = new JTextField("100.00", 18);

    private final JComboBox<String> cmbRisk = new JComboBox<>(new String[]{"CONSERVATIVE", "MODERATE", "AGGRESSIVE"});

    private final FinanceService financeService = FinanceService.getInstance();
    private final Long userId;

    public AddAccountDialog(JFrame parent) {
        super(parent, "Open New Financial Account", true);
        User user = AuthenticationService.getInstance().getCurrentUser();
        this.userId = user != null ? user.getId() : 1L;

        initComponents();
        setupDynamicFormEvents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setResizable(false);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(240, 253, 244));
        JLabel lblHeader = new JLabel("Open New Bank or Investment Account");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(new Color(22, 101, 52));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // Form Body
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(form, gbc, row++, "Account Classification:", cmbType);
        addFormRow(form, gbc, row++, "Account Display Name:", txtAccountName);
        addFormRow(form, gbc, row++, "Initial Deposit ($ USD):", txtInitialBalance);
        addFormRow(form, gbc, row++, lblParam1.getText(), txtParam1);
        addFormRow(form, gbc, row++, lblParam2.getText(), txtParam2);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(new Color(248, 250, 252));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = new JButton("Create Account");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmit.setBackground(new Color(22, 163, 74));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setPreferredSize(new Dimension(140, 32));
        btnSubmit.addActionListener(e -> handleCreateAccount());

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

    private void setupDynamicFormEvents() {
        cmbType.addActionListener(e -> {
            String selected = (String) cmbType.getSelectedItem();
            if ("SAVINGS".equals(selected)) {
                lblParam1.setText("Annual Interest Rate (%):");
                txtParam1.setText("4.50");
                lblParam2.setText("Minimum Balance ($):");
                txtParam2.setText("100.00");
                txtParam2.setVisible(true);
                lblParam2.setVisible(true);
            } else if ("CHECKING".equals(selected)) {
                lblParam1.setText("Overdraft Limit ($):");
                txtParam1.setText("1000.00");
                lblParam2.setVisible(false);
                txtParam2.setVisible(false);
            } else if ("INVESTMENT".equals(selected)) {
                lblParam1.setText("Dividend Yield Rate (%):");
                txtParam1.setText("7.20");
                lblParam2.setText("Portfolio Risk Level:");
                txtParam2.setText("MODERATE");
                txtParam2.setVisible(true);
                lblParam2.setVisible(true);
            }
            revalidate();
            repaint();
        });
    }

    private void handleCreateAccount() {
        String type = (String) cmbType.getSelectedItem();
        String name = txtAccountName.getText().trim();
        String balText = txtInitialBalance.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an account name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal initialBalance;
        try {
            initialBalance = new BigDecimal(balText);
            if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Initial balance must be zero or a positive amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accNumber = ValidationUtil.generateAccountNumber(type.substring(0, 3));
        BigDecimal param1 = new BigDecimal(txtParam1.getText().trim());
        BigDecimal param2 = BigDecimal.ZERO;
        String risk = "MODERATE";

        if ("SAVINGS".equals(type)) {
            param2 = new BigDecimal(txtParam2.getText().trim());
        } else if ("INVESTMENT".equals(type)) {
            risk = txtParam2.getText().trim().toUpperCase();
        }

        try {
            Account account = AccountFactory.createAccount(
                type, null, userId, accNumber, name, initialBalance, "USD",
                param1, param1, param2, risk
            );

            financeService.createAccount(account);
            JOptionPane.showMessageDialog(this, 
                "Account created successfully!\nAccount Number: " + accNumber, 
                "Account Created", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
