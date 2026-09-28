package com.finvantage.ui.dialogs;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.service.DatabaseConnectionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Modal dialog for inspecting and adjusting Oracle Database connectivity settings.
 */
public class DatabaseConfigDialog extends JDialog {

    private final JComboBox<String> cmbMode = new JComboBox<>(new String[]{"ORACLE", "MOCK"});
    private final JTextField txtUrl = new JTextField(24);
    private final JTextField txtUser = new JTextField(15);
    private final JPasswordField txtPassword = new JPasswordField(15);
    private final JLabel lblStatus = new JLabel("Status: Unknown");

    private final DatabaseConfig config = DatabaseConfig.getInstance();
    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();

    public DatabaseConfigDialog(JFrame parent) {
        super(parent, "Oracle Database Configuration", true);
        initComponents();
        loadCurrentConfig();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setResizable(false);

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(241, 245, 249));
        JLabel lblHeader = new JLabel("Oracle Database Connectivity Settings");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(new Color(30, 41, 59));
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(form, gbc, row++, "Persistence Engine:", cmbMode);
        addFormRow(form, gbc, row++, "Oracle JDBC URL:", txtUrl);
        addFormRow(form, gbc, row++, "Database Username:", txtUser);
        addFormRow(form, gbc, row++, "Database Password:", txtPassword);

        // Status Row
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(100, 116, 139));
        form.add(lblStatus, gbc);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        btnPanel.setBackground(new Color(248, 250, 252));

        JButton btnTest = new JButton("Test Connection");
        btnTest.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTest.addActionListener(e -> handleTest());

        JButton btnInitSchema = new JButton("Execute DDL Schema");
        btnInitSchema.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnInitSchema.addActionListener(e -> handleInitSchema());

        JButton btnSave = new JButton("Apply & Close");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnTest);
        btnPanel.add(btnInitSchema);
        btnPanel.add(btnSave);
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

    private void loadCurrentConfig() {
        cmbMode.setSelectedItem(config.getMode().toUpperCase());
        txtUrl.setText(config.getUrl());
        txtUser.setText(config.getUser());
        txtPassword.setText(config.getPassword());
    }

    private void handleTest() {
        applyFormToConfig();
        if (config.isMockMode()) {
            lblStatus.setText("Status: In-Memory Demo Mode Active (No Oracle instance required)");
            lblStatus.setForeground(new Color(22, 163, 74));
            return;
        }

        lblStatus.setText("Status: Testing Oracle connection...");
        lblStatus.setForeground(new Color(217, 119, 6));

        new Thread(() -> {
            boolean ok = dbService.testConnection();
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (ok) {
                    lblStatus.setText("Status: Connection Successful! (Oracle DB Online)");
                    lblStatus.setForeground(new Color(22, 163, 74));
                } else {
                    lblStatus.setText("Status: Connection Failed! Check URL/Credentials.");
                    lblStatus.setForeground(new Color(220, 38, 38));
                }
            });
        }).start();
    }

    private void handleInitSchema() {
        applyFormToConfig();
        if (config.isMockMode()) {
            JOptionPane.showMessageDialog(this, "In-Memory Mock Mode is active. Oracle DDL is bypassed.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema_init.sql")) {
            if (in == null) {
                JOptionPane.showMessageDialog(this, "Schema script not found in classpath resources.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            dbService.executeScript(script);
            JOptionPane.showMessageDialog(this, "Oracle DDL Schema successfully initialized!", "Schema Initialized", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Schema execution encountered an error: " + e.getMessage(), "DDL Execution Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleSave() {
        applyFormToConfig();
        dispose();
    }

    private void applyFormToConfig() {
        config.setMode((String) cmbMode.getSelectedItem());
        config.setUrl(txtUrl.getText().trim());
        config.setUser(txtUser.getText().trim());
        config.setPassword(new String(txtPassword.getPassword()));
    }
}
